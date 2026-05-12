package com.civicconnect.compliance.feign;

import com.civicconnect.compliance.feign.dto.ResolutionValidationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Mirrors ResolutionValidationController in resolution-service.
 * Used to verify a Resolution exists before creating a RESOLUTION-type compliance record.
 */
@FeignClient(name = "resolution-service", fallback = ResolutionFeignClientFallback.class)
public interface ResolutionFeignClient {

    @GetMapping("/internal/resolutions/{resolutionId}")
    ResolutionValidationResponse getResolution(@PathVariable("resolutionId") Long resolutionId);
}
