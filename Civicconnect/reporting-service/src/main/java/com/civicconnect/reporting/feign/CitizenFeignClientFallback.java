package com.civicconnect.reporting.feign;

import com.civicconnect.reporting.exception.ServiceUnavailableException;
import com.civicconnect.reporting.feign.dto.CitizenStatsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CitizenFeignClientFallback implements CitizenFeignClient {
    @Override
    public CitizenStatsResponse getStats() {
        log.warn("[CB] citizen-service unavailable — getStats() throwing ServiceUnavailableException");
        throw new ServiceUnavailableException("citizen-service");
    }
}
