package com.civicconnect.reporting.feign;

import com.civicconnect.reporting.feign.dto.FeedbackStatsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "feedback-service", fallback = FeedbackFeignClientFallback.class)
public interface FeedbackFeignClient {
    @GetMapping("/internal/feedback/stats")
    FeedbackStatsResponse getStats();
}
