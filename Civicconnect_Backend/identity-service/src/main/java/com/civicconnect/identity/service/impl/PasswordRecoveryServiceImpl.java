package com.civicconnect.identity.service.impl;

import com.civicconnect.identity.audit.AuditLogService;
import com.civicconnect.identity.dto.request.*;
import com.civicconnect.identity.dto.response.AdminResetPasswordResponse;
import com.civicconnect.identity.dto.response.PasswordResetTokenResponse;
import com.civicconnect.identity.dto.response.SecurityQuestionResponse;
import com.civicconnect.identity.entity.PasswordResetToken;
import com.civicconnect.identity.entity.SecurityQuestion;
import com.civicconnect.identity.entity.User;
import com.civicconnect.identity.entity.UserSecurityAnswer;
import com.civicconnect.identity.enums.AuditAction;
import com.civicconnect.identity.enums.Role;
import com.civicconnect.identity.enums.UserStatus;
import com.civicconnect.identity.exception.InvalidOperationException;
import com.civicconnect.identity.exception.ResourceNotFoundException;
import com.civicconnect.identity.repository.PasswordResetTokenRepository;
import com.civicconnect.identity.repository.SecurityQuestionRepository;
import com.civicconnect.identity.repository.UserRepository;
import com.civicconnect.identity.repository.UserSecurityAnswerRepository;
import com.civicconnect.identity.service.PasswordRecoveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordRecoveryServiceImpl implements PasswordRecoveryService {

    /** Token lifetime — short enough that a leaked token isn't useful for long. */
    private static final long RESET_TOKEN_TTL_MINUTES = 10;

    /** Admin-issued temporary password length. */
    private static final int TEMP_PASSWORD_LENGTH = 12;

    private static final SecureRandom RANDOM = new SecureRandom();

    // Easy-to-type chars for the temp password (no ambiguous 0/O/I/l/1)
    private static final char[] TEMP_PWD_CHARS =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789".toCharArray();

    private final UserRepository userRepository;
    private final SecurityQuestionRepository securityQuestionRepository;
    private final UserSecurityAnswerRepository answerRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    // ── 1. List all questions ─────────────────────────────────────────────
    @Override
    public List<SecurityQuestionResponse> listAllQuestions() {
        return securityQuestionRepository.findAll().stream()
                .map(q -> SecurityQuestionResponse.builder()
                        .questionId(q.getQuestionId())
                        .questionText(q.getQuestionText())
                        .build())
                .collect(Collectors.toList());
    }

    // ── 2. Look up a user's 3 chosen questions by email ───────────────────
    @Override
    public List<SecurityQuestionResponse> getUserQuestions(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No account found with email: " + request.getEmail()));

        // Only citizens use this flow. Staff must use admin-triggered reset.
        if (user.getRole() != Role.CITIZEN) {
            throw new InvalidOperationException(
                    "Password recovery via security questions is available for citizens only. "
                    + "Staff members should contact a City Administrator to reset their password.");
        }

        List<UserSecurityAnswer> answers = answerRepository.findByUser_UserId(user.getUserId());
        if (answers.isEmpty()) {
            throw new InvalidOperationException(
                    "Security questions are not set up for this account. "
                    + "Please contact a City Administrator for assistance.");
        }

        auditLogService.log(user.getUserId(), AuditAction.PASSWORD_RESET_REQUESTED, "USER",
                String.valueOf(user.getUserId()),
                "Forgot-password flow initiated; questions returned.");

        return answers.stream()
                .map(a -> SecurityQuestionResponse.builder()
                        .questionId(a.getQuestion().getQuestionId())
                        .questionText(a.getQuestion().getQuestionText())
                        .build())
                .collect(Collectors.toList());
    }

    // ── 3. Citizen sets up their 3 security questions ─────────────────────
    @Override
    @Transactional
    public void setupAnswers(Long userId, SecurityAnswersSetupRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + userId));

        if (user.getRole() != Role.CITIZEN) {
            throw new InvalidOperationException(
                    "Security questions are only used by citizens for self-service recovery.");
        }

        // Validate: 3 distinct questions
        Set<Long> questionIds = request.getAnswers().stream()
                .map(SecurityAnswerInput::getQuestionId)
                .collect(Collectors.toCollection(HashSet::new));
        if (questionIds.size() != 3) {
            throw new InvalidOperationException(
                    "Please choose 3 different security questions.");
        }

        // Validate: each questionId exists
        for (Long qId : questionIds) {
            if (!securityQuestionRepository.existsById(qId)) {
                throw new InvalidOperationException("Invalid question ID: " + qId);
            }
        }

        // Replace any existing answers for this user (idempotent re-setup)
        answerRepository.deleteByUser_UserId(userId);

        for (SecurityAnswerInput input : request.getAnswers()) {
            SecurityQuestion question = securityQuestionRepository.findById(input.getQuestionId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Question not found: " + input.getQuestionId()));

            String normalized = normalize(input.getAnswer());
            String hash = passwordEncoder.encode(normalized);

            answerRepository.save(UserSecurityAnswer.builder()
                    .user(user)
                    .question(question)
                    .answerHash(hash)
                    .build());
        }

        auditLogService.log(userId, AuditAction.SECURITY_QUESTIONS_SET, "USER",
                String.valueOf(userId),
                "Citizen set up 3 security questions for password recovery.");
    }

    // ── 4. Verify the 3 answers; on success issue a single-use reset token ─
    @Override
    @Transactional
    public PasswordResetTokenResponse verifyAnswers(VerifySecurityAnswersRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No account found with email: " + request.getEmail()));

        if (user.getRole() != Role.CITIZEN) {
            throw new InvalidOperationException(
                    "Security-question recovery is for citizens only.");
        }

        List<UserSecurityAnswer> stored = answerRepository.findByUser_UserId(user.getUserId());
        if (stored.size() != 3) {
            throw new InvalidOperationException(
                    "Security questions are not properly set up. Contact an administrator.");
        }

        // Index stored answers by questionId for easy lookup
        var storedByQ = stored.stream()
                .collect(Collectors.toMap(a -> a.getQuestion().getQuestionId(), a -> a));

        // EVERY submitted answer must match its stored hash. We don't reveal
        // WHICH answer was wrong — only that one or more are wrong. This
        // prevents an attacker from guessing one question at a time.
        boolean allMatch = true;
        for (SecurityAnswerInput submitted : request.getAnswers()) {
            UserSecurityAnswer storedAnswer = storedByQ.get(submitted.getQuestionId());
            if (storedAnswer == null) {
                allMatch = false;
                break;
            }
            String normalized = normalize(submitted.getAnswer());
            if (!passwordEncoder.matches(normalized, storedAnswer.getAnswerHash())) {
                allMatch = false;
            }
        }

        if (!allMatch) {
            // We don't write a failed audit log here to keep noise low; a
            // proper rate-limiter would track failures separately.
            throw new InvalidOperationException(
                    "One or more answers are incorrect. Please try again.");
        }

        // Issue a single-use, short-lived reset token
        String token = generateSecureToken();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(RESET_TOKEN_TTL_MINUTES);

        PasswordResetToken reset = PasswordResetToken.builder()
                .user(user)
                .token(token)
                .expiresAt(expiry)
                .build();
        tokenRepository.save(reset);

        return PasswordResetTokenResponse.builder()
                .resetToken(token)
                .expiresInSeconds(RESET_TOKEN_TTL_MINUTES * 60)
                .build();
    }

    // ── 5. Consume a reset token and set the new password ────────────────
    @Override
    @Transactional
    public void resetPasswordWithToken(ResetPasswordWithTokenRequest request) {
        PasswordResetToken token = tokenRepository.findByToken(request.getResetToken())
                .orElseThrow(() -> new InvalidOperationException(
                        "Invalid or expired reset token. Please start the recovery process again."));

        if (!token.isUsable()) {
            throw new InvalidOperationException(
                    "This reset token has already been used or has expired. "
                    + "Please start the recovery process again.");
        }

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);

        token.setUsedAt(LocalDateTime.now());
        tokenRepository.save(token);

        auditLogService.log(user.getUserId(), AuditAction.PASSWORD_RESET_COMPLETED, "USER",
                String.valueOf(user.getUserId()),
                "Password reset completed via security-question flow.");
    }

    // ── 6. Authenticated user changes their own password ─────────────────
    @Override
    @Transactional
    public void changeOwnPassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + userId));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidOperationException("Current password is incorrect.");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new InvalidOperationException(
                    "New password must be different from the current password.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);

        auditLogService.log(userId, AuditAction.PASSWORD_CHANGED, "USER",
                String.valueOf(userId),
                "User changed their own password.");
    }

    // ── 7. Admin triggers a forced staff password reset ──────────────────
    @Override
    @Transactional
    public AdminResetPasswordResponse adminResetStaffPassword(Long adminUserId,
                                                              AdminResetPasswordRequest request) {
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Admin user not found: " + adminUserId));
        if (admin.getRole() != Role.CITY_ADMINISTRATOR) {
            throw new InvalidOperationException(
                    "Only a City Administrator can trigger a staff password reset.");
        }

        User target = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Target user not found: " + request.getUserId()));

        // Refuse to reset citizens (they have self-service) or other admins
        // (city admins shouldn't reset each other from the UI; that's a manual
        // sysadmin operation).
        if (target.getRole() == Role.CITIZEN) {
            throw new InvalidOperationException(
                    "Citizens cannot have their password reset by an admin. "
                    + "Citizens use the self-service forgot-password flow with security questions.");
        }
        if (target.getRole() == Role.CITY_ADMINISTRATOR) {
            throw new InvalidOperationException(
                    "City Administrator passwords cannot be reset from the dashboard. "
                    + "Please contact a system administrator.");
        }

        // Refuse to reset suspended staff — suspension is for a reason
        if (target.getStatus() == UserStatus.SUSPENDED) {
            throw new InvalidOperationException(
                    "Cannot reset password for a SUSPENDED user. Activate the account first.");
        }

        String tempPassword = generateTempPassword();
        target.setPassword(passwordEncoder.encode(tempPassword));
        target.setMustChangePassword(true);
        userRepository.save(target);

        auditLogService.log(adminUserId, AuditAction.ADMIN_PASSWORD_RESET, "USER",
                String.valueOf(target.getUserId()),
                "Admin (" + admin.getName() + ", id=" + adminUserId + ") issued temporary password for "
                        + target.getRole() + " " + target.getName() + " (id=" + target.getUserId() + ").");

        return AdminResetPasswordResponse.builder()
                .userId(target.getUserId())
                .name(target.getName())
                .email(target.getEmail())
                .temporaryPassword(tempPassword)
                .message("Temporary password issued. Share with the staff member through a trusted channel. "
                        + "They will be required to change it on next login.")
                .build();
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    /**
     * Normalize answers before hashing/comparing so users aren't punished for
     * capitalization or whitespace.
     */
    private String normalize(String answer) {
        return answer == null ? "" : answer.trim().toLowerCase();
    }

    /**
     * 32 bytes of randomness, URL-safe Base64 encoded. Produces a ~43-char
     * token that is essentially un-guessable.
     */
    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Generates a 12-char temp password drawn from an unambiguous alphabet.
     * Strong enough to resist guessing, short enough to share verbally if
     * needed.
     */
    private String generateTempPassword() {
        StringBuilder sb = new StringBuilder(TEMP_PASSWORD_LENGTH);
        for (int i = 0; i < TEMP_PASSWORD_LENGTH; i++) {
            sb.append(TEMP_PWD_CHARS[RANDOM.nextInt(TEMP_PWD_CHARS.length)]);
        }
        return sb.toString();
    }
}
