package com.civicconnect.citizen.feign;

import com.civicconnect.citizen.exception.ServiceUnavailableException;
import com.civicconnect.citizen.feign.dto.AuditLogRequest;
import com.civicconnect.citizen.feign.dto.UserRegistrationRequest;
import com.civicconnect.citizen.feign.dto.UserValidationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Fallback triggered when identity-service is unavailable or circuit breaker is OPEN.
 */
@Slf4j
@Component
public class IdentityFeignClientFallback implements IdentityFeignClient {

    @Override
    public UserValidationResponse validateUser(Long userId) {
        log.warn("[CB] identity-service unavailable — validateUser({}) throwing ServiceUnavailableException", userId);
        throw new ServiceUnavailableException("identity-service");
    }

    @Override
    public UserValidationResponse validateByEmail(String email) {
        log.warn("[CB] identity-service unavailable — validateByEmail({}) throwing ServiceUnavailableException", email);
        throw new ServiceUnavailableException("identity-service");
    }

    @Override
    public UserValidationResponse registerUser(UserRegistrationRequest request) {
        log.error("[CB] identity-service unavailable — registerUser FAILED for email: {}", request.getEmail());
        throw new ServiceUnavailableException("identity-service");
    }

    @Override
    public void activateUser(Long userId) {
        log.error("[CB] identity-service unavailable — activateUser({}) throwing ServiceUnavailableException", userId);
        throw new ServiceUnavailableException("identity-service");
    }

    @Override
    public void suspendUser(Long userId) {
        log.error("[CB] identity-service unavailable — suspendUser({}) throwing ServiceUnavailableException", userId);
        throw new ServiceUnavailableException("identity-service");
    }

    @Override
    public void writeAuditLog(AuditLogRequest request) {
        // Audit logs are non-critical — just log and continue (don't throw)
        log.warn("[CB] identity-service unavailable — audit log dropped: action={}", request.getAction());
    }

    @Override
    public List<Long> getUserIdsByRole(String role) {
        log.warn("[CB] identity-service unavailable — getUserIdsByRole({}) returning empty", role);
        return Collections.emptyList();
    }
}
