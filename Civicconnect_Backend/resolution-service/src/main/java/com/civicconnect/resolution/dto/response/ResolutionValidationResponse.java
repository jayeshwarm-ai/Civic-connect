package com.civicconnect.resolution.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * Lightweight response for compliance-service Feign calls.
 * GET /internal/resolutions/{resolutionId} returns this.
 */
@Getter
@Builder
public class ResolutionValidationResponse {
    private Long    resolutionId;
    private Long    requestId;
    private Long    officerUserId;
    private String  status;   // ResolutionStatus as String
    private boolean exists;
}
