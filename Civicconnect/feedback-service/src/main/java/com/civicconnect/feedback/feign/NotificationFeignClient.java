package com.civicconnect.feedback.feign;

import com.civicconnect.feedback.feign.dto.SendNotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/** POST /internal/notifications/send — same path as service-request + resolution */
@FeignClient(name = "notification-service", fallback = NotificationFeignClientFallback.class)
public interface NotificationFeignClient {

    @PostMapping("/internal/notifications/send")
    void sendNotification(@RequestBody SendNotificationRequest request);
}
