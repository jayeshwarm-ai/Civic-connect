package com.civicconnect.compliance.feign;

import com.civicconnect.compliance.feign.dto.AuditLogRequest;
import com.civicconnect.compliance.feign.dto.UserValidationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "identity-service", fallback = IdentityFeignClientFallback.class)
public interface IdentityFeignClient {

    @GetMapping("/internal/users/{userId}")
    UserValidationResponse validateUser(@PathVariable("userId") Long userId);

    @PostMapping("/internal/audit-logs")
    void writeAuditLog(@RequestBody AuditLogRequest request);
}
