package com.civicconnect.identity.service.impl;

import com.civicconnect.identity.audit.AuditLogService;
import com.civicconnect.identity.dto.response.AuditLogResponse;
import com.civicconnect.identity.dto.response.UserResponse;
import com.civicconnect.identity.entity.AuditLog;
import com.civicconnect.identity.entity.User;
import com.civicconnect.identity.enums.AuditAction;
import com.civicconnect.identity.enums.UserStatus;
import com.civicconnect.identity.exception.InvalidOperationException;
import com.civicconnect.identity.exception.ResourceNotFoundException;
import com.civicconnect.identity.repository.AuditLogRepository;
import com.civicconnect.identity.repository.UserRepository;
import com.civicconnect.identity.service.IAMService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IAMServiceImpl implements IAMService {

    private final UserRepository     userRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuditLogService    auditLogService;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getMyProfile(Long userId) {
        return mapToUserResponse(findUser(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAllAuditLogs() {
        return auditLogRepository.findAll().stream().map(this::mapToAuditLogResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAuditLogsByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }
        return auditLogService.getLogsForUser(userId).stream().map(this::mapToAuditLogResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAuditLogsByResource(String resource, String resourceId) {
        return auditLogService.getLogsForResource(resource, resourceId).stream()
                .map(this::mapToAuditLogResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAuditLogsByAction(AuditAction action) {
        return auditLogRepository.findByAction(action).stream().map(this::mapToAuditLogResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserResponse deactivateUser(Long targetUserId, Long adminId) {
        User user = findUser(targetUserId);

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new InvalidOperationException("User is already INACTIVE.");
        }

        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);

        auditLogService.log(adminId, AuditAction.USER_DEACTIVATED, "USER",
                String.valueOf(targetUserId), "Deactivated by adminId: " + adminId);

        return mapToUserResponse(user);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    private UserResponse mapToUserResponse(User u) {
        return UserResponse.builder()
                .userId(u.getUserId())
                .name(u.getName())
                .email(u.getEmail())
                .phone(u.getPhone())
                .role(u.getRole())
                .status(u.getStatus())
                .createdAt(u.getCreatedAt())
                .build();
    }

    private AuditLogResponse mapToAuditLogResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .auditId(log.getAuditId())
                .performedBy(log.getPerformedBy())
                .action(log.getAction())
                .resource(log.getResource())
                .resourceId(log.getResourceId())
                .detail(log.getDetail())
                .timestamp(log.getTimestamp())
                .build();
    }
}

