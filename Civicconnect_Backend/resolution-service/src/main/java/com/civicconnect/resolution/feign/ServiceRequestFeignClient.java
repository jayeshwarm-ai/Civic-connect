package com.civicconnect.resolution.feign;

import com.civicconnect.resolution.feign.dto.ServiceRequestValidationResponse;
import com.civicconnect.resolution.feign.dto.StatusUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * Feign client for service-request-service.
 *
 * Mirrors exactly the paths in ServiceRequestValidationController:
 *   GET  /internal/service-requests/{requestId}         → validate request + get status/officer
 *   PATCH /internal/service-requests/{requestId}/status → push IN_PROGRESS / RESOLVED back
 */
@FeignClient(
    name = "service-request-service",
    fallback = ServiceRequestFeignClientFallback.class
)
public interface ServiceRequestFeignClient {

    @GetMapping("/internal/service-requests/{requestId}")
    ServiceRequestValidationResponse getRequest(@PathVariable("requestId") Long requestId);

    @PostMapping("/internal/service-requests/{requestId}/status")
    void updateStatus(@PathVariable("requestId") Long requestId,
                      @RequestBody StatusUpdateRequest request);
}
