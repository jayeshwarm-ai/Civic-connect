package com.civicconnect.compliance.feign.dto;

import lombok.*;

/** Sent to notification-service POST /internal/notifications/send */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SendNotificationRequest {
    private Long   userId;
    private Long   requestId;
    private String message;
    private String category;
}
