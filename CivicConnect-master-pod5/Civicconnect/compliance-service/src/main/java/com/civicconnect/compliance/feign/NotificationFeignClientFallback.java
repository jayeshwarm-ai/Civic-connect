package com.civicconnect.compliance.feign;

import com.civicconnect.compliance.feign.dto.SendNotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationFeignClientFallback implements NotificationFeignClient {

    @Override
    public void sendNotification(SendNotificationRequest request) {
        log.warn("[CB] notification-service unavailable — notification dropped for userId={}", request.getUserId());
    }
}
