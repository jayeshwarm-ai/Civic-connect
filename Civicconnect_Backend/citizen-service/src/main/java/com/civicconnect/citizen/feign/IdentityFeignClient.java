package com.civicconnect.citizen.feign;

import com.civicconnect.citizen.feign.dto.AuditLogRequest;
import com.civicconnect.citizen.feign.dto.UserValidationResponse;
import com.civicconnect.citizen.feign.dto.UserRegistrationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Feign client for identity-service.
 * All paths mirror exactly what identity-service's internal controllers expose.
 *
 * Used by citizen-service to:
 *  1. Check email uniqueness before registration  → GET /internal/users/by-email
 *  2. Create a User in identity-service           → POST /internal/users/register
 *  3. Activate user after document verification   → POST /internal/users/{id}/activate
 *  4. Suspend user when admin deactivates citizen → POST /internal/users/{id}/suspend
 *  5. Validate officer userId on doc review       → GET /internal/users/{id}
 *  6. Write audit log entries                     → POST /internal/audit-logs
 */
@FeignClient(
    name = "identity-service",
    fallback = IdentityFeignClientFallback.class
)
public interface IdentityFeignClient {

    @GetMapping("/internal/users/{userId}")
    UserValidationResponse validateUser(@PathVariable("userId") Long userId);

    @GetMapping("/internal/users/by-email")
    UserValidationResponse validateByEmail(@RequestParam("email") String email);

    @PostMapping("/internal/users/register")
    UserValidationResponse registerUser(@RequestBody UserRegistrationRequest request);

    @PostMapping("/internal/users/{userId}/activate")
    void activateUser(@PathVariable("userId") Long userId);

    @PostMapping("/internal/users/{userId}/suspend")
    void suspendUser(@PathVariable("userId") Long userId);

    @PostMapping("/internal/audit-logs")
    void writeAuditLog(@RequestBody AuditLogRequest request);

    @GetMapping("/internal/users/by-role")
    List<Long> getUserIdsByRole(@RequestParam("role") String role);
}
