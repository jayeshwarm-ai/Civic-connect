package com.civicconnect.servicerequest.feign;

import com.civicconnect.servicerequest.feign.dto.AuditLogRequest;
import com.civicconnect.servicerequest.feign.dto.UserValidationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Feign client for identity-service.
 * Mirrors the exact paths in UserValidationController (/internal/users/**).
 */
@FeignClient(
    name = "identity-service",
    fallbackFactory = IdentityFeignClientFallback.class
)
public interface IdentityFeignClient {

    @GetMapping("/internal/users/{userId}")
    UserValidationResponse validateUser(@PathVariable("userId") Long userId);

    @PostMapping("/internal/audit-logs")
    void writeAuditLog(@RequestBody AuditLogRequest request);

    @GetMapping("/internal/users/by-role")
    List<Long> getUserIdsByRole(@RequestParam("role") String role);
}
