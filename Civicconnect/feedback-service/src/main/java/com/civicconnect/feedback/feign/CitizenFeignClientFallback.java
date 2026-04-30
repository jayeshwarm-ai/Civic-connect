package com.civicconnect.feedback.feign;

import com.civicconnect.feedback.exception.ServiceUnavailableException;
import com.civicconnect.feedback.feign.dto.CitizenValidationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CitizenFeignClientFallback implements CitizenFeignClient {

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
}
