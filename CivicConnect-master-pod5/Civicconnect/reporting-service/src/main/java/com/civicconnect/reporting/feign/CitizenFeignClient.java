package com.civicconnect.reporting.feign;

import com.civicconnect.reporting.feign.dto.CitizenStatsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "citizen-service", fallback = CitizenFeignClientFallback.class)
public interface CitizenFeignClient {
    @GetMapping("/internal/citizens/stats")
    CitizenStatsResponse getStats();
}
