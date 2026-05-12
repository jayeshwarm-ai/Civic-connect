package com.civicconnect.reporting.feign;

import com.civicconnect.reporting.feign.dto.SendNotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service", fallback = NotificationFeignClientFallback.class)
public interface NotificationFeignClient {
    @PostMapping("/internal/notifications/send")
    void sendNotification(@RequestBody SendNotificationRequest request);
}
