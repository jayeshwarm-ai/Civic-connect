package com.civicconnect.reporting.feign;

import com.civicconnect.reporting.feign.dto.ResolutionStatsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "resolution-service", fallback = ResolutionFeignClientFallback.class)
public interface ResolutionFeignClient {
    @GetMapping("/internal/resolutions/stats")
    ResolutionStatsResponse getStats();
}
