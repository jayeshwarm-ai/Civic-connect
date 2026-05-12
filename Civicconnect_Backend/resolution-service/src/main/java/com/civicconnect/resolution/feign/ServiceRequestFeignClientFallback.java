package com.civicconnect.resolution.feign;

import com.civicconnect.resolution.exception.ServiceUnavailableException;
import com.civicconnect.resolution.feign.dto.ServiceRequestValidationResponse;
import com.civicconnect.resolution.feign.dto.StatusUpdateRequest;
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

    @Override
    public void updateStatus(Long requestId, StatusUpdateRequest request) {
        log.error("[CB] service-request-service unavailable — updateStatus({}) FAILED. Status {} not pushed.",
                requestId, request.getStatus());
        // Status sync failure is non-fatal but should be logged
        // Resolution data is saved locally even if status push fails
    }
}
