package com.civicconnect.resolution.feign.dto;

import lombok.*;

/**
 * Mirrors ServiceRequestValidationResponse from service-request-service.
 * Fields match exactly what ServiceRequestValidationController returns.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ServiceRequestValidationResponse {
    private Long   requestId;
    private Long   citizenId;
    private Long   citizenUserId;
    private Long   assignedOfficerUserId;
    private String type;    // ServiceRequestType as String
    private String status;  // ServiceRequestStatus as String
    private boolean exists;
}
