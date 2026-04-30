package com.civicconnect.compliance.feign.dto;

import lombok.*;

/** Mirrors ServiceRequestValidationResponse from service-request-service */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ServiceRequestValidationResponse {
    private Long    requestId;
    private Long    citizenId;
    private Long    citizenUserId;
    private Long    assignedOfficerUserId;
    private String  type;
    private String  status;
    private boolean exists;
}
