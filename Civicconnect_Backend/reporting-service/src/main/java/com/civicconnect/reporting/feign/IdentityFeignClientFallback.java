package com.civicconnect.reporting.feign;

import com.civicconnect.reporting.exception.ServiceUnavailableException;
import com.civicconnect.reporting.feign.dto.AuditLogRequest;
import com.civicconnect.reporting.feign.dto.UserValidationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class IdentityFeignClientFallback implements IdentityFeignClient {
    @Override
    public UserValidationResponse validateUser(Long userId) {
        log.warn("[CB] identity-service unavailable — validateUser({}) throwing ServiceUnavailableException", userId);
        throw new ServiceUnavailableException("identity-service");
    }

    @Override
    public void writeAuditLog(AuditLogRequest request) {
        // Audit logs are non-critical — just log and continue (don't throw)
        log.warn("[CB] identity-service unavailable — audit log dropped");
    }
}
