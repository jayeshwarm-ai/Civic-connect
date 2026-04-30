package com.civicconnect.compliance.feign;

import com.civicconnect.compliance.feign.dto.ServiceRequestValidationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Mirrors ServiceRequestValidationController in service-request-service.
 * Used to verify a ServiceRequest exists before creating a REQUEST-type compliance record.
 */
@FeignClient(name = "service-request-service", fallback = ServiceRequestFeignClientFallback.class)
public interface ServiceRequestFeignClient {

    @GetMapping("/internal/service-requests/{requestId}")
    ServiceRequestValidationResponse getRequest(@PathVariable("requestId") Long requestId);
}
