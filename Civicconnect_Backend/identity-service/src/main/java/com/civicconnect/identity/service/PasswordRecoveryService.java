package com.civicconnect.identity.service;

import com.civicconnect.identity.dto.request.AdminResetPasswordRequest;
import com.civicconnect.identity.dto.request.ChangePasswordRequest;
import com.civicconnect.identity.dto.request.ForgotPasswordRequest;
import com.civicconnect.identity.dto.request.ResetPasswordWithTokenRequest;
import com.civicconnect.identity.dto.request.SecurityAnswersSetupRequest;
import com.civicconnect.identity.dto.request.VerifySecurityAnswersRequest;
import com.civicconnect.identity.dto.response.AdminResetPasswordResponse;
import com.civicconnect.identity.dto.response.PasswordResetTokenResponse;
import com.civicconnect.identity.dto.response.SecurityQuestionResponse;

import java.util.List;

public interface PasswordRecoveryService {

    /** All available security questions (for setup dropdowns). */
    List<SecurityQuestionResponse> listAllQuestions();

    /** A specific user's 3 chosen questions (for the forgot-password flow). */
    List<SecurityQuestionResponse> getUserQuestions(ForgotPasswordRequest request);

    /** Citizen sets up (or replaces) their 3 security questions. */
    void setupAnswers(Long userId, SecurityAnswersSetupRequest request);

    /** Verifies a citizen's answers; on success returns a single-use reset token. */
    PasswordResetTokenResponse verifyAnswers(VerifySecurityAnswersRequest request);

    /** Consumes a reset token and sets a new password. */
    void resetPasswordWithToken(ResetPasswordWithTokenRequest request);

    /** Authenticated user changes their own password (knows the current one). */
    void changeOwnPassword(Long userId, ChangePasswordRequest request);

    /** Admin triggers a forced password reset for a staff member. */
    AdminResetPasswordResponse adminResetStaffPassword(Long adminUserId, AdminResetPasswordRequest request);
}
