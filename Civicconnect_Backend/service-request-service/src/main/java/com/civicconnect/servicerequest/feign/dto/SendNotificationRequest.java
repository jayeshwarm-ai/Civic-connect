package com.civicconnect.servicerequest.feign.dto;

import lombok.*;

/** Sent to notification-service POST /internal/notifications/send */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendNotificationRequest {
    private Long   userId;      // recipient userId
    private Long   requestId;   // related service request
    private String message;
    private String category;    // NotificationCategory as String
}
