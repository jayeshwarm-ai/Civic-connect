package com.civicconnect.notification.feign;

import com.civicconnect.notification.exception.ServiceUnavailableException;
import com.civicconnect.notification.feign.dto.UserValidationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class IdentityFeignClientFallback implements IdentityFeignClient {

    @Override
    public UserValidationResponse validateUser(Long userId) {
        log.warn("[CB] identity-service unavailable — validateUser({}) throwing ServiceUnavailableException", userId);
        throw new ServiceUnavailableException("identity-service");
    }
}
