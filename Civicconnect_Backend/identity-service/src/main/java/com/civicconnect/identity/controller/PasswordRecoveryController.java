package com.civicconnect.identity.controller;

import com.civicconnect.identity.dto.request.AdminResetPasswordRequest;
import com.civicconnect.identity.dto.request.ChangePasswordRequest;
import com.civicconnect.identity.dto.request.ForgotPasswordRequest;
import com.civicconnect.identity.dto.request.ResetPasswordWithTokenRequest;
import com.civicconnect.identity.dto.request.SecurityAnswersSetupRequest;
import com.civicconnect.identity.dto.request.VerifySecurityAnswersRequest;
import com.civicconnect.identity.dto.response.AdminResetPasswordResponse;
import com.civicconnect.identity.dto.response.PasswordResetTokenResponse;
import com.civicconnect.identity.dto.response.SecurityQuestionResponse;
import com.civicconnect.identity.security.JwtUtil;
import com.civicconnect.identity.service.PasswordRecoveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST endpoints for password recovery and password changes.
 *
 * Public endpoints (no JWT required):
 *   GET    /api/v1/auth/security/questions           list available questions
 *   POST   /api/v1/auth/security/forgot-password     get a user's chosen questions
 *   POST   /api/v1/auth/security/verify-answers      verify answers → reset token
 *   POST   /api/v1/auth/security/reset               consume reset token → set password
 *
 * Authenticated endpoints (JWT required):
 *   POST   /api/v1/auth/security/setup               citizen sets up 3 questions
 *   POST   /api/v1/auth/change-password              user changes their own password
 *   POST   /api/v1/auth/admin-reset-password         admin resets a staff member's password
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Password Recovery", description = "Forgot-password and change-password flows")
public class PasswordRecoveryController {

    private final PasswordRecoveryService recoveryService;
    private final JwtUtil jwtUtil;

    // ── Public ────────────────────────────────────────────────────────────

    @Operation(summary = "List available security questions for setup")
    @GetMapping("/security/questions")
    public ResponseEntity<List<SecurityQuestionResponse>> listQuestions() {
        return ResponseEntity.ok(recoveryService.listAllQuestions());
    }

    @Operation(summary = "Step 1 of citizen forgot-password — get the user's 3 chosen questions by email")
    @PostMapping("/security/forgot-password")
    public ResponseEntity<List<SecurityQuestionResponse>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(recoveryService.getUserQuestions(request));
    }

    @Operation(summary = "Step 2 — verify the 3 answers; returns a single-use reset token on success")
    @PostMapping("/security/verify-answers")
    public ResponseEntity<PasswordResetTokenResponse> verifyAnswers(
            @Valid @RequestBody VerifySecurityAnswersRequest request) {
        return ResponseEntity.ok(recoveryService.verifyAnswers(request));
    }

    @Operation(summary = "Step 3 — consume the reset token and set a new password")
    @PostMapping("/security/reset")
    public ResponseEntity<Map<String, String>> resetWithToken(
            @Valid @RequestBody ResetPasswordWithTokenRequest request) {
        recoveryService.resetPasswordWithToken(request);
        return ResponseEntity.ok(Map.of(
                "message", "Password reset successful. Please login with your new password."));
    }

    // ── Authenticated ─────────────────────────────────────────────────────

    @Operation(summary = "Citizen sets up (or replaces) 3 security questions")
    @PostMapping("/security/setup")
    public ResponseEntity<Map<String, String>> setupAnswers(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody SecurityAnswersSetupRequest request) {
        Long userId = extractUserId(authHeader);
        recoveryService.setupAnswers(userId, request);
        return ResponseEntity.ok(Map.of(
                "message", "Security questions saved. You can now use them to recover your password."));
    }

    @Operation(summary = "Authenticated user changes their own password (must know current password)")
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ChangePasswordRequest request) {
        Long userId = extractUserId(authHeader);
        recoveryService.changeOwnPassword(userId, request);
        return ResponseEntity.ok(Map.of(
                "message", "Password changed successfully."));
    }

    @Operation(summary = "City Administrator resets a staff member's password (issues a temporary password)")
    @PostMapping("/admin-reset-password")
    public ResponseEntity<AdminResetPasswordResponse> adminResetPassword(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody AdminResetPasswordRequest request) {
        Long adminUserId = extractUserId(authHeader);
        return ResponseEntity.ok(recoveryService.adminResetStaffPassword(adminUserId, request));
    }

    // ── Helper ────────────────────────────────────────────────────────────

    private Long extractUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new com.civicconnect.identity.exception.InvalidOperationException(
                    "Authorization header missing or malformed.");
        }
        return jwtUtil.extractUserId(authHeader.substring(7));
    }
}
