package com.civicconnect.servicerequest.controller;

import com.civicconnect.servicerequest.dto.response.ServiceRequestValidationResponse;
import com.civicconnect.servicerequest.dto.request.StatusUpdateInternalRequest;
import com.civicconnect.servicerequest.entity.RequestUpdate;
import com.civicconnect.servicerequest.entity.ServiceRequest;
import com.civicconnect.servicerequest.repository.RequestUpdateRepository;
import com.civicconnect.servicerequest.repository.ServiceRequestRepository;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Internal-only API consumed by resolution-service and feedback-service via Feign.
 *
 * NOT in Swagger (@Hidden). NOT behind JWT. Accessible service-to-service only.
 *
 * Used by:
 *  - resolution-service → get request details + update status to IN_PROGRESS / RESOLVED
 *  - feedback-service   → verify request is CLOSED before accepting feedback
 */
@Hidden
@RestController
@RequestMapping("/internal/service-requests")
@RequiredArgsConstructor
public class ServiceRequestValidationController {

    private final ServiceRequestRepository serviceRequestRepository;
    private final RequestUpdateRepository  requestUpdateRepository;

    /** Get request details for validation */
    @GetMapping("/{requestId}")
    public ResponseEntity<ServiceRequestValidationResponse> getRequest(
            @PathVariable Long requestId) {
        return serviceRequestRepository.findById(requestId)
                .map(r -> ResponseEntity.ok(mapToValidation(r, true)))
                .orElseGet(() -> ResponseEntity.ok(
                        ServiceRequestValidationResponse.builder()
                                .requestId(requestId)
                                .exists(false)
                                .build()));
    }

    /**
     * Update request status — called by resolution-service when a resolution is
     * created (→ IN_PROGRESS) or all workflow steps complete (→ RESOLVED).
     *
     * Also writes a RequestUpdate audit row so the change is visible in the
     * citizen's update-history view (same way the direct officer-update path
     * does). Requires actor info on the request body.
     */
    @PostMapping("/{requestId}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long requestId,
            @RequestBody StatusUpdateInternalRequest request) {
        serviceRequestRepository.findById(requestId).ifPresent(sr -> {
            sr.setStatus(request.getStatus());
            serviceRequestRepository.save(sr);

            // Write audit row only if we have an actor (defensive: fall back to
            // skipping rather than violating NOT NULL on officer_user_id).
            if (request.getOfficerUserId() != null) {
                RequestUpdate update = RequestUpdate.builder()
                        .serviceRequest(sr)
                        .officerUserId(request.getOfficerUserId())
                        .officerName(request.getOfficerName() != null
                                ? request.getOfficerName()
                                : "Officer#" + request.getOfficerUserId())
                        .notes(request.getNotes() != null && !request.getNotes().isBlank()
                                ? request.getNotes()
                                : "Status changed to " + request.getStatus())
                        .status(request.getStatus())
                        .build();
                requestUpdateRepository.save(update);
            }
        });
        return ResponseEntity.noContent().build();
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    private ServiceRequestValidationResponse mapToValidation(ServiceRequest r, boolean exists) {
        return ServiceRequestValidationResponse.builder()
                .requestId(r.getRequestId())
                .citizenId(r.getCitizenId())
                .citizenUserId(r.getCitizenUserId())
                .assignedOfficerUserId(r.getAssignedOfficerUserId())
                .type(r.getType())
                .status(r.getStatus())
                .exists(exists)
                .build();
    }
}
