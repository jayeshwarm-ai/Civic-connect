package com.civicconnect.identity.controller;

import com.civicconnect.identity.dto.request.CreateAuditRecordRequest;
import com.civicconnect.identity.dto.request.UpdateAuditRecordRequest;
import com.civicconnect.identity.dto.response.AuditRecordResponse;
import com.civicconnect.identity.enums.AuditStatus;
import com.civicconnect.identity.service.AuditRecordService;
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
@RequestMapping("/api/v1/audit-records")
@RequiredArgsConstructor
@Tag(name = "Audit Records", description = "Formal compliance audits — COMPLIANCE_OFFICER & CITY_ADMINISTRATOR")
@SecurityRequirement(name = "BearerAuth")
public class AuditRecordController {

    private final AuditRecordService auditRecordService;

    // ── POST /api/v1/audit-records ────────────────────────────────────────────
    @Operation(
        summary = "Create audit record",
        description = "Compliance Officer initiates a new formal audit with a defined scope."
    )
    @ApiResponse(responseCode = "201", description = "Audit record created")
    @PreAuthorize("hasAnyRole('COMPLIANCE_OFFICER','CITY_ADMINISTRATOR')")
    @PostMapping
    public ResponseEntity<AuditRecordResponse> createAuditRecord(
            @Valid @RequestBody CreateAuditRecordRequest request,
            Authentication authentication) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(auditRecordService.createAuditRecord(request, extractUserId(authentication)));
    }

    // ── PATCH /api/v1/audit-records/{auditId} ─────────────────────────────────
    @Operation(
        summary = "Update audit record status and findings",
        description = "Move the audit forward: OPEN → IN_REVIEW → CLOSED. " +
                      "Only the officer who created the record can update it."
    )
    @ApiResponse(responseCode = "200", description = "Audit record updated")
    @ApiResponse(responseCode = "400", description = "Invalid status transition or not the owning officer")
    @ApiResponse(responseCode = "404", description = "Audit record not found")
    @PreAuthorize("hasAnyRole('COMPLIANCE_OFFICER','CITY_ADMINISTRATOR')")
    @PatchMapping("/{auditId}")
    public ResponseEntity<AuditRecordResponse> updateAuditRecord(
            @PathVariable Long auditId,
            @Valid @RequestBody UpdateAuditRecordRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(
                auditRecordService.updateAuditRecord(auditId, request, extractUserId(authentication)));
    }

    // ── GET /api/v1/audit-records/{auditId} ───────────────────────────────────
    @Operation(summary = "Get audit record by ID")
    @ApiResponse(responseCode = "200", description = "Audit record found")
    @ApiResponse(responseCode = "404", description = "Audit record not found")
    @PreAuthorize("hasAnyRole('COMPLIANCE_OFFICER','CITY_ADMINISTRATOR')")
    @GetMapping("/{auditId}")
    public ResponseEntity<AuditRecordResponse> getById(@PathVariable Long auditId) {
        return ResponseEntity.ok(auditRecordService.getById(auditId));
    }

    // ── GET /api/v1/audit-records ─────────────────────────────────────────────
    @Operation(summary = "Get all audit records")
    @ApiResponse(responseCode = "200", description = "List returned")
    @PreAuthorize("hasAnyRole('COMPLIANCE_OFFICER','CITY_ADMINISTRATOR')")
    @GetMapping
    public ResponseEntity<List<AuditRecordResponse>> getAll() {
        return ResponseEntity.ok(auditRecordService.getAll());
    }

    // ── GET /api/v1/audit-records/officer/{officerId} ─────────────────────────
    @Operation(summary = "Get audit records by officer")
    @ApiResponse(responseCode = "200", description = "List returned")
    @PreAuthorize("hasAnyRole('COMPLIANCE_OFFICER','CITY_ADMINISTRATOR')")
    @GetMapping("/officer/{officerId}")
    public ResponseEntity<List<AuditRecordResponse>> getByOfficer(@PathVariable Long officerId) {
        return ResponseEntity.ok(auditRecordService.getByOfficer(officerId));
    }

    // ── GET /api/v1/audit-records/status?status=OPEN ──────────────────────────
    @Operation(summary = "Get audit records by status")
    @ApiResponse(responseCode = "200", description = "List returned")
    @PreAuthorize("hasAnyRole('COMPLIANCE_OFFICER','CITY_ADMINISTRATOR')")
    @GetMapping("/status")
    public ResponseEntity<List<AuditRecordResponse>> getByStatus(
            @Parameter(description = "OPEN, IN_REVIEW, or CLOSED")
            @RequestParam AuditStatus status) {
        return ResponseEntity.ok(auditRecordService.getByStatus(status));
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    private Long extractUserId(Authentication auth) {
        return ((Number) ((UsernamePasswordAuthenticationToken) auth).getDetails()).longValue();
    }
}
