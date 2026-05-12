package com.civicconnect.resolution.feign;

import com.civicconnect.resolution.exception.ServiceUnavailableException;
import com.civicconnect.resolution.feign.dto.AuditLogRequest;
import com.civicconnect.resolution.feign.dto.UserValidationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class IdentityFeignClientFallback implements IdentityFeignClient {

    @Override
    public UserValidationResponse validateUser(Long userId) {
        log.warn("[CB] identity-service unavailable — validateUser({}) throwing ServiceUnavailableException", userId);
        throw new ServiceUnavailableException("identity-service");
    }

    @Override
    public List<Long> findUserIdsByRole(String role) {
        // Used only for non-critical broadcast notifications — degrade gracefully (empty list)
        log.warn("[CB] identity-service unavailable — findUserIdsByRole({}) returning empty list", role);
        return Collections.emptyList();
    }

    @Override
    public void writeAuditLog(AuditLogRequest request) {
        // Audit logs are non-critical — just log and continue (don't throw)
        log.warn("[CB] identity-service unavailable — audit log dropped: action={}", request.getAction());
    }
}
