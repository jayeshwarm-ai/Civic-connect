package com.civicconnect.resolution.feign.dto;

import lombok.*;

/**
 * Mirrors StatusUpdateInternalRequest from service-request-service.
 * Sent via POST /internal/service-requests/{requestId}/status.
 *
 * Carries the actor + notes so service-request-service can write a meaningful
 * RequestUpdate audit row alongside the status change (visible to the citizen).
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StatusUpdateRequest {
    private String status;          // ServiceRequestStatus as String e.g. "IN_PROGRESS", "RESOLVED"
    private Long   officerUserId;
    private String officerName;     // pre-formatted with role suffix
    private String notes;
}
