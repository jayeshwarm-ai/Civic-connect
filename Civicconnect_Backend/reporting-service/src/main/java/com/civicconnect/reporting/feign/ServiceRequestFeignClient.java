package com.civicconnect.reporting.feign;

import com.civicconnect.reporting.feign.dto.ServiceRequestStatsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "service-request-service", fallback = ServiceRequestFeignClientFallback.class)
public interface ServiceRequestFeignClient {
    @GetMapping("/internal/service-requests/stats")
    ServiceRequestStatsResponse getStats();
}
