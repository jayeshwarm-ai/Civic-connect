package com.civicconnect.compliance.feign;

import com.civicconnect.compliance.exception.ServiceUnavailableException;
import com.civicconnect.compliance.feign.dto.ServiceRequestValidationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ServiceRequestFeignClientFallback implements ServiceRequestFeignClient {

    @Override
    public ServiceRequestValidationResponse getRequest(Long requestId) {
        log.warn("[CB] service-request-service unavailable — getRequest({}) throwing ServiceUnavailableException", requestId);
        throw new ServiceUnavailableException("service-request-service");
    }
}
