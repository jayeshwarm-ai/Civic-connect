package com.civicconnect.notification.dto.request;

import lombok.Getter;
import lombok.Setter;

/**
 * Received from all other microservices via POST /internal/notifications/send.
 * Mirrors the SendNotificationRequest Feign DTO in every other service.
 * category is a String (enum name) because it comes from different service packages.
 */
@Getter
@Setter
public class SendNotificationRequest {
    private Long   userId;      // recipient
    private Long   requestId;   // nullable — related service request
    private String message;
    private String category;    // NotificationCategory enum name as String
}
