package com.civicconnect.servicerequest.feign.dto;

import lombok.*;

/**
 * Mirrors CitizenValidationResponse from citizen-service.
 * Fields match exactly what citizen-service's CitizenValidationController returns.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CitizenValidationResponse {
    private Long   citizenId;
    private Long   userId;
    private String name;
    private String email;
    private String accountStatus;   // UserStatus as String (INACTIVE / ACTIVE / SUSPENDED)
    private boolean exists;
}
