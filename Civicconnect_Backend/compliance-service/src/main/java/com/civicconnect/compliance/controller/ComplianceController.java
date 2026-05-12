package com.civicconnect.compliance.controller;

import com.civicconnect.compliance.dto.request.CreateAuditRecordRequest;
import com.civicconnect.compliance.dto.request.CreateComplianceRecordRequest;
import com.civicconnect.compliance.dto.request.UpdateAuditRecordRequest;
import com.civicconnect.compliance.dto.response.AuditRecordResponse;
import com.civicconnect.compliance.dto.response.ComplianceRecordResponse;
import com.civicconnect.compliance.enums.AuditStatus;
import com.civicconnect.compliance.enums.ComplianceResult;
import com.civicconnect.compliance.enums.ComplianceType;
import com.civicconnect.compliance.service.ComplianceService;
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
@RequestMapping("/api/v1/compliance")
@RequiredArgsConstructor
@Tag(name = "Compliance & Audit",
     description = "Compliance records and audit management — COMPLIANCE_OFFICER and CITY_ADMINISTRATOR only")
@SecurityRequirement(name = "BearerAuth")
public class ComplianceController {

    private final ComplianceService complianceService;

    // ── POST /api/v1/compliance/records ───────────────────────────────────────
    @Operation(
        summary = "Create compliance record — COMPLIANCE_OFFICER only",
        description = "Creates an immutable PASS/FAIL compliance record for a ServiceRequest or Resolution. "
                    + "Notifies the officer on FAIL."
    )
    @ApiResponse(responseCode = "201", description = "Compliance record created")
    @ApiResponse(responseCode = "400", description = "Not COMPLIANCE_OFFICER role")
    @ApiResponse(responseCode = "404", description = "Entity not found")
    @PreAuthorize("hasAnyRole('COMPLIANCE_OFFICER','CITY_ADMINISTRATOR')")
    @PostMapping("/records")
    public ResponseEntity<ComplianceRecordResponse> createComplianceRecord(
            @Valid @RequestBody CreateComplianceRecordRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(complianceService.createComplianceRecord(
                        request, extractUserId(authentication)));
    }

    // ── GET /api/v1/compliance/records/{complianceId} ─────────────────────────
    @Operation(summary = "Get compliance record by ID")
    @ApiResponse(responseCode = "200", description = "Record found")
    @ApiResponse(responseCode = "404", description = "Record not found")
    @PreAuthorize("hasAnyRole('COMPLIANCE_OFFICER','CITY_ADMINISTRATOR')")
    @GetMapping("/records/{complianceId}")
    public ResponseEntity<ComplianceRecordResponse> getComplianceRecordById(
            @PathVariable Long complianceId) {
        return ResponseEntity.ok(complianceService.getComplianceRecordById(complianceId));
    }

    // ── GET /api/v1/compliance/records?type=REQUEST&entityId=5 ────────────────
    @Operation(summary = "Get compliance records for a specific entity")
    @PreAuthorize("hasAnyRole('COMPLIANCE_OFFICER','CITY_ADMINISTRATOR')")
    @GetMapping("/records")
    public ResponseEntity<List<ComplianceRecordResponse>> getComplianceRecordsByEntity(
            @RequestParam ComplianceType type,
            @RequestParam Long entityId) {
        return ResponseEntity.ok(
                complianceService.getComplianceRecordsByEntity(type, entityId));
    }

    // ── GET /api/v1/compliance/records/result?result=FAIL ────────────────────
    @Operation(summary = "Get compliance records by result (PASS or FAIL)")
    @PreAuthorize("hasAnyRole('COMPLIANCE_OFFICER','CITY_ADMINISTRATOR')")
    @GetMapping("/records/result")
    public ResponseEntity<List<ComplianceRecordResponse>> getComplianceRecordsByResult(
            @RequestParam ComplianceResult result) {
        return ResponseEntity.ok(complianceService.getComplianceRecordsByResult(result));
    }

    // ── POST /api/v1/compliance/audits ────────────────────────────────────────
    @Operation(
        summary = "Create audit record — COMPLIANCE_OFFICER only",
        description = "Creates an audit with status OPEN. Transitions: OPEN → IN_REVIEW → CLOSED."
    )
    @ApiResponse(responseCode = "201", description = "Audit created")
    @ApiResponse(responseCode = "400", description = "Not COMPLIANCE_OFFICER role")
    @PreAuthorize("hasAnyRole('COMPLIANCE_OFFICER','CITY_ADMINISTRATOR')")
    @PostMapping("/audits")
    public ResponseEntity<AuditRecordResponse> createAuditRecord(
            @Valid @RequestBody CreateAuditRecordRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(complianceService.createAuditRecord(
                        request, extractUserId(authentication)));
    }

    // ── GET /api/v1/compliance/audits/{auditId} ───────────────────────────────
    @Operation(summary = "Get audit record by ID")
    @PreAuthorize("hasAnyRole('COMPLIANCE_OFFICER','CITY_ADMINISTRATOR')")
    @GetMapping("/audits/{auditId}")
    public ResponseEntity<AuditRecordResponse> getAuditRecordById(@PathVariable Long auditId) {
        return ResponseEntity.ok(complianceService.getAuditRecordById(auditId));
    }

    // ── PATCH /api/v1/compliance/audits/{auditId} ─────────────────────────────
    @Operation(
        summary = "Update audit status and findings — creating officer only",
        description = "Forward-only: OPEN → IN_REVIEW → CLOSED. Cannot update CLOSED audits."
    )
    @ApiResponse(responseCode = "200", description = "Audit updated")
    @ApiResponse(responseCode = "400", description = "Invalid transition or CLOSED audit")
    @PreAuthorize("hasAnyRole('COMPLIANCE_OFFICER','CITY_ADMINISTRATOR')")
    @PatchMapping("/audits/{auditId}")
    public ResponseEntity<AuditRecordResponse> updateAuditRecord(
            @PathVariable Long auditId,
            @Valid @RequestBody UpdateAuditRecordRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(complianceService.updateAuditRecord(
                auditId, request, extractUserId(authentication)));
    }

    // ── GET /api/v1/compliance/audits/officer/{officerId} ────────────────────
    @Operation(summary = "Get all audits by officer")
    @PreAuthorize("hasAnyRole('COMPLIANCE_OFFICER','CITY_ADMINISTRATOR')")
    @GetMapping("/audits/officer/{officerId}")
    public ResponseEntity<List<AuditRecordResponse>> getAuditsByOfficerId(
            @PathVariable Long officerId) {
        return ResponseEntity.ok(complianceService.getAuditsByOfficerId(officerId));
    }

    // ── GET /api/v1/compliance/audits?status=OPEN ─────────────────────────────
    @Operation(summary = "Get audits by status")
    @PreAuthorize("hasAnyRole('COMPLIANCE_OFFICER','CITY_ADMINISTRATOR')")
    @GetMapping("/audits")
    public ResponseEntity<List<AuditRecordResponse>> getAuditsByStatus(
            @RequestParam AuditStatus status) {
        return ResponseEntity.ok(complianceService.getAuditsByStatus(status));
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    private Long extractUserId(Authentication auth) {
        return ((Number) ((UsernamePasswordAuthenticationToken) auth).getDetails()).longValue();
    }
}
