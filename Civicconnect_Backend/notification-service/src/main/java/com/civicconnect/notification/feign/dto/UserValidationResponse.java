package com.civicconnect.notification.feign.dto;

import lombok.*;

/** Mirrors UserValidationResponse from identity-service */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserValidationResponse {
    private Long    userId;
    private String  name;
    private String  email;
    private String  role;
    private String  status;
    private boolean exists;
}
