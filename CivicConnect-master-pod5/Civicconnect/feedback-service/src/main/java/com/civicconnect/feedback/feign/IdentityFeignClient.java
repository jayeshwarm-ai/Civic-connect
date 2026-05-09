package com.civicconnect.feedback.feign;

import com.civicconnect.feedback.feign.dto.AuditLogRequest;
import com.civicconnect.feedback.feign.dto.UserValidationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign client for identity-service.
 * GET  /internal/users/{userId}  — validate officer exists
 * POST /internal/audit-logs      — write audit trail
 */
@FeignClient(name = "identity-service", fallback = IdentityFeignClientFallback.class)
public interface IdentityFeignClient {

    @GetMapping("/internal/users/{userId}")
    UserValidationResponse validateUser(@PathVariable("userId") Long userId);

    @PostMapping("/internal/audit-logs")
    void writeAuditLog(@RequestBody AuditLogRequest request);
}
