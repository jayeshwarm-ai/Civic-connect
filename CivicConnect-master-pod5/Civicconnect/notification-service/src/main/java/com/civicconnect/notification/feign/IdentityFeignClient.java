package com.civicconnect.notification.feign;

import com.civicconnect.notification.feign.dto.UserValidationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client for identity-service.
 * Used to validate userId before querying notifications for that user.
 * Mirrors exactly UserValidationController in identity-service.
 */
@FeignClient(name = "identity-service", fallback = IdentityFeignClientFallback.class)
public interface IdentityFeignClient {

    @GetMapping("/internal/users/{userId}")
    UserValidationResponse validateUser(@PathVariable("userId") Long userId);
}
