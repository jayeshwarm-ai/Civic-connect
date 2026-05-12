package com.civicconnect.feedback.feign.dto;

import lombok.*;

/** Mirrors CitizenValidationResponse from citizen-service exactly */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CitizenValidationResponse {
    private Long    citizenId;
    private Long    userId;
    private String  name;
    private String  email;
    private String  accountStatus;  // UserStatus as String
    private boolean exists;
}
