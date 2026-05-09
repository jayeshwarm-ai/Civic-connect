package com.civicconnect.feedback.feign;

import com.civicconnect.feedback.exception.ServiceUnavailableException;
import com.civicconnect.feedback.feign.dto.ServiceRequestValidationResponse;
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
