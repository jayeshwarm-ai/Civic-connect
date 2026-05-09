package com.civicconnect.resolution.feign.dto;

import lombok.*;

/**
 * Mirrors StatusUpdateInternalRequest from service-request-service.
 * Sent via PATCH /internal/service-requests/{requestId}/status
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StatusUpdateRequest {
    private String status;  // ServiceRequestStatus as String e.g. "IN_PROGRESS", "RESOLVED"
}
