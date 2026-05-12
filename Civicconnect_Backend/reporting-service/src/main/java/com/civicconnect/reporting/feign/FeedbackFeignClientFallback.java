package com.civicconnect.reporting.feign;

import com.civicconnect.reporting.exception.ServiceUnavailableException;
import com.civicconnect.reporting.feign.dto.FeedbackStatsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FeedbackFeignClientFallback implements FeedbackFeignClient {
    @Override
    public FeedbackStatsResponse getStats() {
        log.warn("[CB] feedback-service unavailable — getStats() throwing ServiceUnavailableException");
        throw new ServiceUnavailableException("feedback-service");
    }
}
