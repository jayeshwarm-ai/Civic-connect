package com.civicconnect.reporting.feign;

import com.civicconnect.reporting.feign.dto.ComplianceStatsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "compliance-service", fallback = ComplianceFeignClientFallback.class)
public interface ComplianceFeignClient {
    @GetMapping("/internal/compliance/stats")
    ComplianceStatsResponse getStats();
}
