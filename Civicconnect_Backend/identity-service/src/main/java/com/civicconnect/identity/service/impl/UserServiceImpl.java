package com.civicconnect.identity.service.impl;

import com.civicconnect.identity.audit.AuditLogService;
import com.civicconnect.identity.dto.request.CreateStaffRequest;
import com.civicconnect.identity.dto.request.UpdateMyProfileRequest;
import com.civicconnect.identity.dto.response.StaffResponse;
import com.civicconnect.identity.entity.User;
import com.civicconnect.identity.enums.AuditAction;
import com.civicconnect.identity.enums.Role;
import com.civicconnect.identity.enums.UserStatus;
import com.civicconnect.identity.exception.DuplicateResourceException;
import com.civicconnect.identity.exception.InvalidOperationException;
import com.civicconnect.identity.exception.ResourceNotFoundException;
import com.civicconnect.identity.repository.UserRepository;
import com.civicconnect.identity.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository  userRepository;
    private final AuditLogService auditLogService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public StaffResponse createStaff(CreateStaffRequest request, Long adminId) {
        if (request.getRole() == Role.CITIZEN) {
            throw new InvalidOperationException("CITIZEN accounts cannot be created via this endpoint. Use /citizens/register.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateResourceException("Phone already registered: " + request.getPhone());
        }

        User staff = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(request.getRole())
                .status(UserStatus.ACTIVE)
                .build();

        staff = userRepository.save(staff);

        auditLogService.log(adminId, AuditAction.STAFF_CREATED, "USER",
                String.valueOf(staff.getUserId()), "Staff created with role: " + request.getRole());

        return mapToStaffResponse(staff);
    }

    @Override
    @Transactional
    public StaffResponse registerStaffPublic(CreateStaffRequest request) {
        if (request.getRole() == Role.CITIZEN) {
            throw new InvalidOperationException("CITIZEN accounts must be created via /citizens/register.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateResourceException("Phone already registered: " + request.getPhone());
        }

        User staff = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(request.getRole())
                .status(UserStatus.ACTIVE)
                .build();

        staff = userRepository.save(staff);

        auditLogService.log(staff.getUserId(), AuditAction.STAFF_CREATED, "USER",
                String.valueOf(staff.getUserId()), "Self-registered as: " + request.getRole());

        return mapToStaffResponse(staff);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffResponse> getStaffByRole(Role role) {
        if (role == Role.CITIZEN) {
            throw new InvalidOperationException("Use citizen endpoints to query citizen accounts.");
        }
        return userRepository.findByRole(role).stream().map(this::mapToStaffResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public StaffResponse getStaffById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        if (user.getRole() == Role.CITIZEN) {
            throw new InvalidOperationException("Use citizen endpoints to query citizen accounts.");
        }
        return mapToStaffResponse(user);
    }

    @Override
    @Transactional
    public StaffResponse updateStaffStatus(Long userId, UserStatus newStatus, Long adminId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (user.getRole() == Role.CITIZEN) {
            throw new InvalidOperationException("Use citizen endpoints to manage citizen accounts.");
        }
        if (user.getStatus() == newStatus) {
            throw new InvalidOperationException("User is already " + newStatus);
        }

        user.setStatus(newStatus);
        userRepository.save(user);

        auditLogService.log(adminId, AuditAction.STAFF_STATUS_UPDATED, "USER",
                String.valueOf(userId), "Status updated to: " + newStatus + " by adminId: " + adminId);

        return mapToStaffResponse(user);
    }

    /**
     * Returns the current authenticated user's own profile.
     * Works for any staff role. Citizens use the citizen-service profile API instead.
     */
    @Override
    public StaffResponse getMyProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return mapToStaffResponse(user);
    }

    /**
     * Lets a staff user (Service Officer / Department Head / Compliance Officer)
     * update their own email and phone.
     *
     * Rules:
     *   • CITY_ADMINISTRATOR cannot use this endpoint — admin profiles are
     *     managed centrally and cannot be self-edited.
     *   • CITIZEN should never reach this endpoint (their JWT role isn't allowed
     *     by the controller's @PreAuthorize), but we double-check here.
     *   • Email and phone uniqueness is preserved; trying to take another user's
     *     email or phone is rejected with 409.
     */
    @Override
    @Transactional
    public StaffResponse updateMyProfile(Long userId, UpdateMyProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.getRole() == Role.CITY_ADMINISTRATOR) {
            throw new InvalidOperationException("City Administrator profiles cannot be self-edited.");
        }
        if (user.getRole() == Role.CITIZEN) {
            throw new InvalidOperationException("Citizens must use the citizen-service profile endpoint.");
        }

        // Uniqueness checks — only complain when the value is changing
        if (!user.getEmail().equalsIgnoreCase(request.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already in use: " + request.getEmail());
        }
        if (!user.getPhone().equals(request.getPhone())
                && userRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateResourceException("Phone already in use: " + request.getPhone());
        }

        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        userRepository.save(user);

        auditLogService.log(userId, AuditAction.STAFF_PROFILE_UPDATED, "USER",
                String.valueOf(userId),
                "Self-update of email/phone by userId: " + userId);

        return mapToStaffResponse(user);
    }

    private StaffResponse mapToStaffResponse(User u) {
        return StaffResponse.builder()
                .userId(u.getUserId())
                .name(u.getName())
                .email(u.getEmail())
                .phone(u.getPhone())
                .role(u.getRole())
                .status(u.getStatus())
                .createdAt(u.getCreatedAt())
                .build();
    }
}

