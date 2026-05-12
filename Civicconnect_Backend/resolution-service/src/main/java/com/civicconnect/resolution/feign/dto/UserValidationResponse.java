package com.civicconnect.resolution.feign.dto;

import lombok.*;

/**
 * Mirrors UserValidationResponse from identity-service.
 * Fields match exactly what UserValidationController returns.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserValidationResponse {
    private Long   userId;
    private String name;
    private String email;
    private String role;    // Role as String e.g. "SERVICE_OFFICER"
    private String status;  // UserStatus as String
    private boolean exists;
}
