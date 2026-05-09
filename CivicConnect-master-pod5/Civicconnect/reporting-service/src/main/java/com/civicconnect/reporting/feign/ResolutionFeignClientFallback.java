package com.civicconnect.reporting.feign;

import com.civicconnect.reporting.exception.ServiceUnavailableException;
import com.civicconnect.reporting.feign.dto.ResolutionStatsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ResolutionFeignClientFallback implements ResolutionFeignClient {
    @Override
    public ResolutionStatsResponse getStats() {
        log.warn("[CB] resolution-service unavailable — getStats() throwing ServiceUnavailableException");
        throw new ServiceUnavailableException("resolution-service");
    }
}
