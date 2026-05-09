package com.civicconnect.feedback.feign;

import com.civicconnect.feedback.feign.dto.ServiceRequestValidationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client for service-request-service.
 * GET /internal/service-requests/{requestId} — verify CLOSED + get assignedOfficerUserId
 */
@FeignClient(name = "service-request-service", fallback = ServiceRequestFeignClientFallback.class)
public interface ServiceRequestFeignClient {

    @GetMapping("/internal/service-requests/{requestId}")
    ServiceRequestValidationResponse getRequest(@PathVariable("requestId") Long requestId);
}
