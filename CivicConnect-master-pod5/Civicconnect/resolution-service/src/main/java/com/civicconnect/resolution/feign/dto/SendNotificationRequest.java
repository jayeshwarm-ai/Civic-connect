package com.civicconnect.resolution.feign.dto;

import lombok.*;

/**
 * Mirrors SendNotificationRequest from service-request-service.
 * Sent to notification-service POST /internal/notifications/send
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SendNotificationRequest {
    private Long   userId;      // recipient userId
    private Long   requestId;   // related service request
    private String message;
    private String category;    // NotificationCategory as String
}
