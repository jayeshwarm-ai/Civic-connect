package com.civicconnect.reporting.feign;

import com.civicconnect.reporting.exception.ServiceUnavailableException;
import com.civicconnect.reporting.feign.dto.ComplianceStatsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ComplianceFeignClientFallback implements ComplianceFeignClient {
    @Override
    public ComplianceStatsResponse getStats() {
        log.warn("[CB] compliance-service unavailable — getStats() throwing ServiceUnavailableException");
        throw new ServiceUnavailableException("compliance-service");
    }
}
