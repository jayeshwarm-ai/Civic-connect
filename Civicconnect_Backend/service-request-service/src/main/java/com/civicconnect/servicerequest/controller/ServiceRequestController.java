package com.civicconnect.servicerequest.controller;

import com.civicconnect.servicerequest.dto.request.AssignOfficerRequest;
import com.civicconnect.servicerequest.dto.request.RequestStatusUpdateRequest;
import com.civicconnect.servicerequest.dto.request.ServiceRequestSubmitRequest;
import com.civicconnect.servicerequest.dto.response.RequestUpdateResponse;
import com.civicconnect.servicerequest.dto.response.ServiceRequestResponse;
import com.civicconnect.servicerequest.enums.ServiceRequestStatus;
import com.civicconnect.servicerequest.service.ServiceRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/service-requests")
@RequiredArgsConstructor
@Tag(name = "Service Request Management",
     description = "Submit, track, and manage service requests")
@SecurityRequirement(name = "BearerAuth")
public class ServiceRequestController {

    private final ServiceRequestService serviceRequestService;

    // ── POST /api/v1/service-requests — CITIZEN only ──────────────────────────
    @Operation(
        summary = "Submit service request — CITIZEN only",
        description = "Citizen must have ACTIVE account (documents verified). "
                    + "citizenId is auto-resolved from the JWT — no need to pass it manually."
    )
    @ApiResponse(responseCode = "201", description = "Request submitted")
    @ApiResponse(responseCode = "400", description = "Account not ACTIVE")
    @ApiResponse(responseCode = "404", description = "Citizen not found for this user")
    @PreAuthorize("hasRole('CITIZEN')")
    @PostMapping
    public ResponseEntity<ServiceRequestResponse> submitRequest(
            @Valid @RequestBody ServiceRequestSubmitRequest request,
            Authentication authentication) {
        // citizenId is resolved from JWT userId inside the service — cleaner API
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(serviceRequestService.submitRequest(
                        extractUserId(authentication), request));
    }

    // ── PATCH /{requestId}/close — CITIZEN only ───────────────────────────────
    @Operation(
        summary = "Close a resolved request — CITIZEN only",
        description = "Citizen confirms resolution. Request must be in RESOLVED status."
    )
    @ApiResponse(responseCode = "200", description = "Request closed")
    @ApiResponse(responseCode = "400", description = "Not RESOLVED or not your request")
    @PreAuthorize("hasRole('CITIZEN')")
    @PatchMapping("/{requestId}/close")
    public ResponseEntity<ServiceRequestResponse> closeRequest(
            @PathVariable Long requestId, Authentication authentication) {
        return ResponseEntity.ok(
                serviceRequestService.closeRequest(requestId, extractUserId(authentication)));
    }

    // ── DELETE /{requestId} — CITIZEN only ───────────────────────────────────
    @Operation(
        summary = "Withdraw a submitted request — CITIZEN only",
        description = "Only SUBMITTED (unassigned) requests can be withdrawn."
    )
    @ApiResponse(responseCode = "204", description = "Request withdrawn")
    @ApiResponse(responseCode = "400", description = "Not SUBMITTED or not your request")
    @PreAuthorize("hasRole('CITIZEN')")
    @DeleteMapping("/{requestId}")
    public ResponseEntity<Void> withdrawRequest(
            @PathVariable Long requestId, Authentication authentication) {
        serviceRequestService.withdrawRequest(requestId, extractUserId(authentication));
        return ResponseEntity.noContent().build();
    }

    // ── PUT /{requestId} — CITIZEN only (edit SUBMITTED request) ────────────
    @Operation(
        summary = "Edit a submitted request — CITIZEN only",
        description = "Citizen can edit type, description, state, city, and address of a SUBMITTED request."
    )
    @ApiResponse(responseCode = "200", description = "Request updated")
    @ApiResponse(responseCode = "400", description = "Not SUBMITTED or not your request")
    @PreAuthorize("hasRole('CITIZEN')")
    @PutMapping("/{requestId}")
    public ResponseEntity<ServiceRequestResponse> updateRequest(
            @PathVariable Long requestId,
            @Valid @RequestBody ServiceRequestSubmitRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(
                serviceRequestService.updateRequest(requestId, extractUserId(authentication), request));
    }

    // ── GET /my-requests — CITIZEN only ────────────────────────────────────────
    @Operation(
        summary = "Get all my service requests — CITIZEN only",
        description = "Returns all service requests submitted by the logged-in citizen. "
                    + "citizenId is auto-resolved from the JWT."
    )
    @ApiResponse(responseCode = "200", description = "Requests found")
    @ApiResponse(responseCode = "404", description = "Citizen profile not found")
    @PreAuthorize("hasRole('CITIZEN')")
    @GetMapping("/my-requests")
    public ResponseEntity<List<ServiceRequestResponse>> getMyRequests(Authentication authentication) {
        return ResponseEntity.ok(
                serviceRequestService.getMyRequests(extractUserId(authentication)));
    }

    // ── GET /{requestId} — CITIZEN | SERVICE_OFFICER | DEPARTMENT_HEAD | ADMIN ──
    @Operation(
        summary = "Get service request by ID",
        description = "Citizens can only view their own requests. Officers and admins can view any request."
    )
    @ApiResponse(responseCode = "200", description = "Request found")
    @ApiResponse(responseCode = "403", description = "This request is not yours")
    @ApiResponse(responseCode = "404", description = "Request not found")
    @PreAuthorize("hasAnyRole('CITIZEN','SERVICE_OFFICER','DEPARTMENT_HEAD','CITY_ADMINISTRATOR','COMPLIANCE_OFFICER')")
    @GetMapping("/{requestId}")
    public ResponseEntity<ServiceRequestResponse> getRequestById(
            @PathVariable Long requestId,
            Authentication authentication) {
        return ResponseEntity.ok(serviceRequestService.getRequestById(
                requestId, extractUserId(authentication), authentication.getAuthorities()));
    }

    // ── GET /citizen/{citizenId} — DEPARTMENT_HEAD | ADMIN only ────────────────
    @Operation(
        summary = "Get all requests by citizen ID",
        description = "DEPARTMENT_HEAD and CITY_ADMINISTRATOR only can view any citizen's requests."
    )
    @PreAuthorize("hasAnyRole('DEPARTMENT_HEAD','CITY_ADMINISTRATOR')")
    @GetMapping("/citizen/{citizenId}")
    public ResponseEntity<List<ServiceRequestResponse>> getRequestsByCitizenId(
            @PathVariable Long citizenId) {
        return ResponseEntity.ok(serviceRequestService.getRequestsByCitizenId(citizenId));
    }

    // ── GET /{requestId}/updates — CITIZEN | SERVICE_OFFICER | DEPARTMENT_HEAD | ADMIN ─────
    @Operation(
        summary = "Get update history for a request",
        description = "Citizens can only view updates for their own requests. Officers and admins can view any request updates."
    )
    @ApiResponse(responseCode = "403", description = "This request is not yours")
    @PreAuthorize("hasAnyRole('CITIZEN','SERVICE_OFFICER','DEPARTMENT_HEAD','CITY_ADMINISTRATOR','COMPLIANCE_OFFICER')")
    @GetMapping("/{requestId}/updates")
    public ResponseEntity<List<RequestUpdateResponse>> getRequestUpdates(
            @PathVariable Long requestId,
            Authentication authentication) {
        return ResponseEntity.ok(serviceRequestService.getRequestUpdates(
                requestId, extractUserId(authentication), authentication.getAuthorities()));
    }

    // ── GET /officer/{officerId} — SERVICE_OFFICER+ ───────────────────────────
    @Operation(summary = "Get requests assigned to an officer")
    @PreAuthorize("hasAnyRole('SERVICE_OFFICER','DEPARTMENT_HEAD','CITY_ADMINISTRATOR')")
    @GetMapping("/officer/{officerId}")
    public ResponseEntity<List<ServiceRequestResponse>> getRequestsByOfficerId(
            @PathVariable Long officerId) {
        return ResponseEntity.ok(serviceRequestService.getRequestsByOfficerId(officerId));
    }

    // ── GET /?status=SUBMITTED — SERVICE_OFFICER+ ─────────────────────────────
    @Operation(
        summary = "Get requests filtered by status",
        description = "SERVICE_OFFICER sees only their assigned requests. "
                    + "DEPARTMENT_HEAD and CITY_ADMINISTRATOR see all requests."
    )
    @PreAuthorize("hasAnyRole('SERVICE_OFFICER','DEPARTMENT_HEAD','CITY_ADMINISTRATOR','COMPLIANCE_OFFICER')")
    @GetMapping
    public ResponseEntity<List<ServiceRequestResponse>> getRequestsByStatus(
            @Parameter(description = "SUBMITTED | ASSIGNED | IN_PROGRESS | RESOLVED | CLOSED")
            @RequestParam ServiceRequestStatus status,
            Authentication authentication) {
        return ResponseEntity.ok(serviceRequestService.getRequestsByStatus(
                status, extractUserId(authentication), authentication.getAuthorities()));
    }

    // ── PATCH /{requestId}/status — SERVICE_OFFICER+ ──────────────────────────
    @Operation(
        summary = "Update request status — assigned officer only",
        description = "Moves: ASSIGNED → IN_PROGRESS → RESOLVED. Forward-only transitions enforced."
    )
    @ApiResponse(responseCode = "200", description = "Status updated")
    @ApiResponse(responseCode = "400", description = "Invalid transition or not assigned officer")
    @PreAuthorize("hasAnyRole('SERVICE_OFFICER','DEPARTMENT_HEAD','CITY_ADMINISTRATOR')")
    @PatchMapping("/{requestId}/status")
    public ResponseEntity<ServiceRequestResponse> updateRequestStatus(
            @PathVariable Long requestId,
            @Valid @RequestBody RequestStatusUpdateRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(
                serviceRequestService.updateRequestStatus(
                        requestId, request, extractUserId(authentication)));
    }

    // ── PATCH /{requestId}/assign — DEPARTMENT_HEAD | ADMIN ──────────────────
    @Operation(
        summary = "Assign officer to request — DEPARTMENT_HEAD and CITY_ADMINISTRATOR only",
        description = "Assigns a SERVICE_OFFICER to a SUBMITTED request. "
                    + "Creates first RequestUpdate entry."
    )
    @ApiResponse(responseCode = "200", description = "Officer assigned")
    @ApiResponse(responseCode = "400", description = "Request not SUBMITTED or user not SERVICE_OFFICER")
    @ApiResponse(responseCode = "404", description = "Request or officer not found")
    @PreAuthorize("hasAnyRole('DEPARTMENT_HEAD','CITY_ADMINISTRATOR')")
    @PatchMapping("/{requestId}/assign")
    public ResponseEntity<ServiceRequestResponse> assignOfficer(
            @PathVariable Long requestId,
            @Valid @RequestBody AssignOfficerRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(
                serviceRequestService.assignOfficer(
                        requestId, request, extractUserId(authentication)));
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    private Long extractUserId(Authentication auth) {
        return ((Number) ((UsernamePasswordAuthenticationToken) auth).getDetails()).longValue();
    }
}
