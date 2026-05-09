package com.civicconnect.servicerequest.controller;

import com.civicconnect.servicerequest.dto.response.ServiceRequestValidationResponse;
import com.civicconnect.servicerequest.dto.request.StatusUpdateInternalRequest;
import com.civicconnect.servicerequest.entity.ServiceRequest;
import com.civicconnect.servicerequest.enums.ServiceRequestStatus;
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

    /** Update request status — called by resolution-service when all workflow steps complete */
    @PostMapping("/{requestId}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long requestId,
            @RequestBody StatusUpdateInternalRequest request) {
        serviceRequestRepository.findById(requestId).ifPresent(sr -> {
            sr.setStatus(request.getStatus());
            serviceRequestRepository.save(sr);
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
