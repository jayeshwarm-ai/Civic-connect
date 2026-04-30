package com.civicconnect.servicerequest.feign;

import com.civicconnect.servicerequest.feign.dto.SendNotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign client for notification-service.
 * Used to send in-app notifications on every status change.
 */
@FeignClient(
    name = "notification-service",
    fallbackFactory = NotificationFeignClientFallback.class
)
public interface NotificationFeignClient {

    @PostMapping("/internal/notifications/send")
    void sendNotification(@RequestBody SendNotificationRequest request);
}
