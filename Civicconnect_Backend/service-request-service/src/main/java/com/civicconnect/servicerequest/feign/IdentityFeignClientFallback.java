package com.civicconnect.servicerequest.feign;

import com.civicconnect.servicerequest.exception.ServiceUnavailableException;
import com.civicconnect.servicerequest.feign.dto.AuditLogRequest;
import com.civicconnect.servicerequest.feign.dto.UserValidationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class IdentityFeignClientFallback implements FallbackFactory<IdentityFeignClient> {

    @Override
    public IdentityFeignClient create(Throwable cause) {
        log.error("[CB] identity-service fallback triggered due to: {}", cause.getMessage());
        
        return new IdentityFeignClient() {
            @Override
            public UserValidationResponse validateUser(Long userId) {
                log.warn("[CB] identity-service unavailable — validateUser({}) throwing ServiceUnavailableException", userId);
                throw new ServiceUnavailableException("identity-service");
            }

            @Override
            public void writeAuditLog(AuditLogRequest request) {
                log.warn("[CB] identity-service unavailable — audit log dropped: action={}, resource={}",
                        request.getAction(), request.getResource());
            }

            @Override
            public List<Long> getUserIdsByRole(String role) {
                log.warn("[CB] identity-service unavailable — getUserIdsByRole({}) returning empty", role);
                return Collections.emptyList();
            }
        };
    }
}
