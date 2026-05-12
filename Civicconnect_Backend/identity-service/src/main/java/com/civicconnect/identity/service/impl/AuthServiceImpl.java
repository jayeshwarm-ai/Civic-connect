package com.civicconnect.identity.service.impl;

import com.civicconnect.identity.audit.AuditLogService;
import com.civicconnect.identity.dto.request.LoginRequest;
import com.civicconnect.identity.dto.request.ResetPasswordRequest;
import com.civicconnect.identity.dto.response.LoginResponse;
import com.civicconnect.identity.entity.User;
import com.civicconnect.identity.enums.AuditAction;
import com.civicconnect.identity.enums.UserStatus;
import com.civicconnect.identity.exception.InvalidOperationException;
import com.civicconnect.identity.exception.ResourceNotFoundException;
import com.civicconnect.identity.repository.UserRepository;
import com.civicconnect.identity.security.JwtUtil;
import com.civicconnect.identity.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil         jwtUtil;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invalid credentials — no account found for: " + request.getEmail()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidOperationException("Invalid credentials — incorrect password.");
        }

        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new InvalidOperationException("Account is SUSPENDED. Please contact the administrator.");
        }

        String token = jwtUtil.generateToken(user.getUserId(), user.getEmail(), user.getRole());

        auditLogService.log(user.getUserId(), AuditAction.USER_LOGIN, "USER",
                String.valueOf(user.getUserId()), "Login successful. Role: " + user.getRole());

        return LoginResponse.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .token(token)
                .tokenType("Bearer")
                .build();
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No account found for email: " + request.getEmail()));

        if (!user.getPhone().equals(request.getPhone())) {
            throw new InvalidOperationException("Phone number does not match our records.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        auditLogService.log(user.getUserId(), AuditAction.USER_STATUS_UPDATED, "USER",
                String.valueOf(user.getUserId()), "Password reset via email + phone verification.");
    }
}

