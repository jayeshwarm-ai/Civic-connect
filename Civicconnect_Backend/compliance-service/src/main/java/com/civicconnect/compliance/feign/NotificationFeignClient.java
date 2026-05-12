package com.civicconnect.compliance.feign;

import com.civicconnect.compliance.feign.dto.SendNotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/** POST /internal/notifications/send — same path across all services */
@FeignClient(name = "notification-service", fallback = NotificationFeignClientFallback.class)
public interface NotificationFeignClient {

    @PostMapping("/internal/notifications/send")
    void sendNotification(@RequestBody SendNotificationRequest request);
}
