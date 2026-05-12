package com.civicconnect.compliance.feign.dto;

import lombok.*;

/** Mirrors ResolutionValidationResponse from resolution-service */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ResolutionValidationResponse {
    private Long    resolutionId;
    private Long    requestId;
    private Long    officerUserId;
    private String  status;
    private boolean exists;
}
