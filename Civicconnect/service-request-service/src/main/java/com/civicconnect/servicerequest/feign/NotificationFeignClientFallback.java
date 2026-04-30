package com.civicconnect.servicerequest.feign;

import com.civicconnect.servicerequest.feign.dto.SendNotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationFeignClientFallback implements FallbackFactory<NotificationFeignClient> {

    @Override
    public NotificationFeignClient create(Throwable cause) {
        log.error("[CB] notification-service fallback triggered due to: {}", cause.getMessage());
        
        return new NotificationFeignClient() {
            @Override
            public void sendNotification(SendNotificationRequest request) {
                log.warn("[CB] notification-service unavailable — notification dropped for userId={}",
                        request.getUserId());
                // Non-critical: notifications are best-effort — never break the main flow
            }
        };
    }
}
