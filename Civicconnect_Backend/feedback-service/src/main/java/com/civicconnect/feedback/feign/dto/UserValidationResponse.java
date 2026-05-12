package com.civicconnect.feedback.feign.dto;

import lombok.*;

/** Mirrors UserValidationResponse from identity-service exactly */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserValidationResponse {
    private Long    userId;
    private String  name;
    private String  email;
    private String  role;
    private String  status;
    private boolean exists;
}
