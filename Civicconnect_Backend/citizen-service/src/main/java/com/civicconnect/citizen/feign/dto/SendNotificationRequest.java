package com.civicconnect.citizen.feign.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendNotificationRequest {
    private Long   userId;
    private Long   requestId;
    private String message;
    private String category;
}

