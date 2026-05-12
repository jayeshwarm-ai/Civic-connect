package com.civicconnect.feedback.feign;

import com.civicconnect.feedback.feign.dto.CitizenValidationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client for citizen-service.
 * GET /internal/citizens/{citizenId}  — verify citizenId + get citizen name
 * GET /internal/citizens/by-user/{userId} — resolve citizenId from JWT userId
 */
@FeignClient(name = "citizen-service", fallback = CitizenFeignClientFallback.class)
public interface CitizenFeignClient {

    @GetMapping("/internal/citizens/{citizenId}")
    CitizenValidationResponse validateCitizen(@PathVariable("citizenId") Long citizenId);

    @GetMapping("/internal/citizens/by-user/{userId}")
    CitizenValidationResponse getCitizenByUserId(@PathVariable("userId") Long userId);
}
