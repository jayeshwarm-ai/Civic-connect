package com.civicconnect.compliance.feign;

import com.civicconnect.compliance.exception.ServiceUnavailableException;
import com.civicconnect.compliance.feign.dto.ResolutionValidationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ResolutionFeignClientFallback implements ResolutionFeignClient {

    @Override
    public ResolutionValidationResponse getResolution(Long resolutionId) {
        log.warn("[CB] resolution-service unavailable — getResolution({}) throwing ServiceUnavailableException", resolutionId);
        throw new ServiceUnavailableException("resolution-service");
    }
}
