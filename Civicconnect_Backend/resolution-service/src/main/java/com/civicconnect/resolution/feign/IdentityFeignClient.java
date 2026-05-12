package com.civicconnect.resolution.feign;

import com.civicconnect.resolution.feign.dto.AuditLogRequest;
import com.civicconnect.resolution.feign.dto.UserValidationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Feign client for identity-service.
 *
 * Mirrors exactly the paths in UserValidationController and AuditLogInternalController:
 *   GET  /internal/users/{userId}    → validate officer/step-assignee existence + role
 *   GET  /internal/users/by-role     → fetch userIds for a given role (e.g. COMPLIANCE_OFFICER)
 *   POST /internal/audit-logs        → write audit trail entries
 */
@FeignClient(
    name = "identity-service",
    fallback = IdentityFeignClientFallback.class
)
public interface IdentityFeignClient {

    @GetMapping("/internal/users/{userId}")
    UserValidationResponse validateUser(@PathVariable("userId") Long userId);

    @GetMapping("/internal/users/by-role")
    List<Long> findUserIdsByRole(@RequestParam("role") String role);

    @PostMapping("/internal/audit-logs")
    void writeAuditLog(@RequestBody AuditLogRequest request);
}
