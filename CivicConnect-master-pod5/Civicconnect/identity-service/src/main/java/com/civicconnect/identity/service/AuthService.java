package com.civicconnect.identity.service;

import com.civicconnect.identity.dto.request.LoginRequest;
import com.civicconnect.identity.dto.request.ResetPasswordRequest;
import com.civicconnect.identity.dto.response.LoginResponse;

/**
 * Service interface for Authentication operations.
 */
public interface AuthService {
    LoginResponse login(LoginRequest request);
    void resetPassword(ResetPasswordRequest request);
}
