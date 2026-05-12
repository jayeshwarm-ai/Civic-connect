package com.civicconnect.reporting.feign;

import com.civicconnect.reporting.exception.ServiceUnavailableException;
import com.civicconnect.reporting.feign.dto.ServiceRequestStatsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ServiceRequestFeignClientFallback implements ServiceRequestFeignClient {
    @Override
    public ServiceRequestStatsResponse getStats() {
        log.warn("[CB] service-request-service unavailable — getStats() throwing ServiceUnavailableException");
        throw new ServiceUnavailableException("service-request-service");
    }
}
