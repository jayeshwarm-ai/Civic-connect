package com.civicconnect.resolution.controller;

import com.civicconnect.resolution.dto.request.CreateResolutionRequest;
import com.civicconnect.resolution.dto.request.CreateWorkflowStepRequest;
import com.civicconnect.resolution.dto.request.UpdateWorkflowStepRequest;
import com.civicconnect.resolution.dto.response.ResolutionResponse;
import com.civicconnect.resolution.dto.response.WorkflowStepResponse;
import com.civicconnect.resolution.service.ResolutionService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/v1/resolutions")
@RequiredArgsConstructor
@Tag(name = "Resolution Management",
     description = "Issue resolution and workflow step management — SERVICE_OFFICER and above")
@SecurityRequirement(name = "BearerAuth")
public class ResolutionController {

    private final ResolutionService resolutionService;

    // ── POST /api/v1/resolutions ───────────────────────────────────────────────
    @Operation(
        summary = "Create resolution — SERVICE_OFFICER and above",
        description = "Assigned officer creates a resolution for an ASSIGNED service request. "
                    + "Automatically pushes IN_PROGRESS status to service-request-service."
    )
    @ApiResponse(responseCode = "201", description = "Resolution created")
    @ApiResponse(responseCode = "400", description = "Request not ASSIGNED, not the assigned officer, or resolution already exists")
    @ApiResponse(responseCode = "404", description = "Service request not found")
    @PreAuthorize("hasAnyRole('SERVICE_OFFICER','DEPARTMENT_HEAD','CITY_ADMINISTRATOR')")
    @PostMapping
    public ResponseEntity<ResolutionResponse> createResolution(
            @Valid @RequestBody CreateResolutionRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(resolutionService.createResolution(request, extractUserId(authentication)));
    }

    // ── GET /api/v1/resolutions/{resolutionId} ────────────────────────────────
    @Operation(summary = "Get resolution by ID")
    @ApiResponse(responseCode = "200", description = "Resolution found")
    @ApiResponse(responseCode = "404", description = "Resolution not found")
    @PreAuthorize("hasAnyRole('SERVICE_OFFICER','DEPARTMENT_HEAD','CITY_ADMINISTRATOR','COMPLIANCE_OFFICER')")
    @GetMapping("/{resolutionId}")
    public ResponseEntity<ResolutionResponse> getResolutionById(@PathVariable Long resolutionId) {
        return ResponseEntity.ok(resolutionService.getResolutionById(resolutionId));
    }

    // ── GET /api/v1/resolutions/by-request/{requestId} ────────────────────────
    @Operation(summary = "Get resolution by service request ID")
    @ApiResponse(responseCode = "200", description = "Resolution found")
    @ApiResponse(responseCode = "404", description = "No resolution for this request")
    @PreAuthorize("hasAnyRole('SERVICE_OFFICER','DEPARTMENT_HEAD','CITY_ADMINISTRATOR','COMPLIANCE_OFFICER')")
    @GetMapping("/by-request/{requestId}")
    public ResponseEntity<ResolutionResponse> getResolutionByRequestId(
            @PathVariable Long requestId) {
        return ResponseEntity.ok(resolutionService.getResolutionByRequestId(requestId));
    }

    // ── GET /api/v1/resolutions/officer/{officerId} ───────────────────────────
    @Operation(summary = "Get all resolutions by officer")
    @ApiResponse(responseCode = "200", description = "List returned")
    @PreAuthorize("hasAnyRole('SERVICE_OFFICER','DEPARTMENT_HEAD','CITY_ADMINISTRATOR','COMPLIANCE_OFFICER')")
    @GetMapping("/officer/{officerId}")
    public ResponseEntity<List<ResolutionResponse>> getResolutionsByOfficerId(
            @PathVariable Long officerId) {
        return ResponseEntity.ok(resolutionService.getResolutionsByOfficerId(officerId));
    }

    // ── POST /api/v1/resolutions/{resolutionId}/steps ─────────────────────────
    @Operation(
        summary = "Add workflow step — resolution officer only",
        description = "Adds a step to an IN_PROGRESS resolution. "
                    + "Step assignee must be a SERVICE_OFFICER."
    )
    @ApiResponse(responseCode = "201", description = "Step created")
    @ApiResponse(responseCode = "400", description = "Resolution not IN_PROGRESS, not the owning officer, or assignee not SERVICE_OFFICER")
    @PreAuthorize("hasAnyRole('SERVICE_OFFICER','DEPARTMENT_HEAD','CITY_ADMINISTRATOR')")
    @PostMapping("/{resolutionId}/steps")
    public ResponseEntity<WorkflowStepResponse> addWorkflowStep(
            @PathVariable Long resolutionId,
            @Valid @RequestBody CreateWorkflowStepRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(resolutionService.addWorkflowStep(
                        resolutionId, request, extractUserId(authentication)));
    }

    // ── GET /api/v1/resolutions/{resolutionId}/steps ──────────────────────────
    @Operation(summary = "Get all workflow steps for a resolution")
    @ApiResponse(responseCode = "200", description = "Steps returned")
    @PreAuthorize("hasAnyRole('SERVICE_OFFICER','DEPARTMENT_HEAD','CITY_ADMINISTRATOR','COMPLIANCE_OFFICER')")
    @GetMapping("/{resolutionId}/steps")
    public ResponseEntity<List<WorkflowStepResponse>> getWorkflowSteps(
            @PathVariable Long resolutionId) {
        return ResponseEntity.ok(resolutionService.getWorkflowStepsByResolutionId(resolutionId));
    }

    // ── PATCH /api/v1/resolutions/steps/{stepId}/status ───────────────────────
    @Operation(
        summary = "Update workflow step status — assigned officer only",
        description = "Transitions: PENDING → IN_PROGRESS → COMPLETED. "
                    + "When all steps complete, resolution is auto-completed and "
                    + "service request is pushed to RESOLVED."
    )
    @ApiResponse(responseCode = "200", description = "Step status updated")
    @ApiResponse(responseCode = "400", description = "Not assigned user or invalid transition")
    @PreAuthorize("hasAnyRole('SERVICE_OFFICER','DEPARTMENT_HEAD','CITY_ADMINISTRATOR')")
    @PatchMapping("/steps/{stepId}/status")
    public ResponseEntity<WorkflowStepResponse> updateWorkflowStepStatus(
            @PathVariable Long stepId,
            @Valid @RequestBody UpdateWorkflowStepRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(resolutionService.updateWorkflowStepStatus(
                stepId, request, extractUserId(authentication)));
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    private Long extractUserId(Authentication auth) {
        return ((Number) ((UsernamePasswordAuthenticationToken) auth).getDetails()).longValue();
    }
}
