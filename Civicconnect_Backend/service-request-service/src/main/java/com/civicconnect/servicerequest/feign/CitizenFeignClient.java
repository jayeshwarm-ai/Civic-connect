package com.civicconnect.servicerequest.feign;

import com.civicconnect.servicerequest.feign.dto.CitizenValidationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client for citizen-service.
 * Mirrors the exact paths in CitizenValidationController (/internal/citizens/**).
 */

@FeignClient(
    name = "citizen-service",
    fallbackFactory = CitizenFeignClientFallback.class
)
public interface CitizenFeignClient {

    /** Validate citizenId and get account status */
    @GetMapping("/internal/citizens/{citizenId}")
    CitizenValidationResponse validateCitizen(@PathVariable("citizenId") Long citizenId);

    /** Look up citizen by userId (from JWT) */
    @GetMapping("/internal/citizens/by-user/{userId}")
    CitizenValidationResponse getCitizenByUserId(@PathVariable("userId") Long userId);
}
