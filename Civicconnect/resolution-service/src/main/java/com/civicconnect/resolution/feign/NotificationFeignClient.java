package com.civicconnect.resolution.feign;

import com.civicconnect.resolution.feign.dto.SendNotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign client for notification-service.
 * Path mirrors exactly what service-request-service uses:
 *   POST /internal/notifications/send
 */
@FeignClient(
    name = "notification-service",
    fallback = NotificationFeignClientFallback.class
)
public interface NotificationFeignClient {

    @PostMapping("/internal/notifications/send")
    void sendNotification(@RequestBody SendNotificationRequest request);
}
