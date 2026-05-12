package com.civicconnect.servicerequest.feign.dto;

import lombok.*;

/**
 * Mirrors UserValidationResponse from identity-service.
 * Fields match exactly what identity-service's UserValidationController returns.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserValidationResponse {
    private Long   userId;
    private String name;
    private String email;
    private String role;    // Role as String (CITIZEN / SERVICE_OFFICER / etc.)
    private String status;  // UserStatus as String
    private boolean exists;
}
