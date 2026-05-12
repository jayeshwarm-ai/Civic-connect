package com.civicconnect.servicerequest.feign;

import com.civicconnect.servicerequest.exception.ServiceUnavailableException;
import com.civicconnect.servicerequest.feign.dto.CitizenValidationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CitizenFeignClientFallback implements FallbackFactory<CitizenFeignClient> {

    @Override
    public CitizenFeignClient create(Throwable cause) {
        log.error("[CB] citizen-service fallback triggered due to: {}", cause.getMessage());
        
        return new CitizenFeignClient() {
            @Override
            public CitizenValidationResponse validateCitizen(Long citizenId) {
                log.warn("[CB] citizen-service unavailable — validateCitizen({}) throwing ServiceUnavailableException", citizenId);
                throw new ServiceUnavailableException("citizen-service");
            }

            @Override
            public CitizenValidationResponse getCitizenByUserId(Long userId) {
                log.warn("[CB] citizen-service unavailable — getCitizenByUserId({}) throwing ServiceUnavailableException", userId);
                throw new ServiceUnavailableException("citizen-service");
            }
        };
    }
}
