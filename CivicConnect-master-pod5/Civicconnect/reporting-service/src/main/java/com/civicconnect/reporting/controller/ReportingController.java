package com.civicconnect.reporting.controller;

import com.civicconnect.reporting.dto.request.GenerateReportRequest;
import com.civicconnect.reporting.dto.response.ReportResponse;
import com.civicconnect.reporting.enums.ReportScope;
import com.civicconnect.reporting.service.ReportingService;
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
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Generate and retrieve system reports")
@SecurityRequirement(name = "BearerAuth")
public class ReportingController {

    private final ReportingService reportingService;

    // ── POST /api/v1/reports — DEPARTMENT_HEAD | CITY_ADMINISTRATOR ───────────
    @Operation(
        summary = "Generate report — DEPARTMENT_HEAD and CITY_ADMINISTRATOR only",
        description = "Aggregates live data from all services. Scopes: REQUEST, FEEDBACK, COMPLIANCE, DEPARTMENT."
    )
    @ApiResponse(responseCode = "201", description = "Report generated")
    @ApiResponse(responseCode = "400", description = "Unauthorised role")
    @PreAuthorize("hasAnyRole('DEPARTMENT_HEAD','CITY_ADMINISTRATOR')")
    @PostMapping
    public ResponseEntity<ReportResponse> generateReport(
            @Valid @RequestBody GenerateReportRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reportingService.generateReport(request, extractUserId(authentication)));
    }

    // ── GET /api/v1/reports/{reportId} ────────────────────────────────────────
    @Operation(summary = "Get report by ID")
    @ApiResponse(responseCode = "200", description = "Report found")
    @ApiResponse(responseCode = "404", description = "Report not found")
    @PreAuthorize("hasAnyRole('DEPARTMENT_HEAD','CITY_ADMINISTRATOR','COMPLIANCE_OFFICER')")
    @GetMapping("/{reportId}")
    public ResponseEntity<ReportResponse> getReportById(@PathVariable Long reportId) {
        return ResponseEntity.ok(reportingService.getReportById(reportId));
    }

    // ── GET /api/v1/reports ───────────────────────────────────────────────────
    @Operation(summary = "Get all reports — newest first")
    @PreAuthorize("hasAnyRole('DEPARTMENT_HEAD','CITY_ADMINISTRATOR','COMPLIANCE_OFFICER')")
    @GetMapping
    public ResponseEntity<List<ReportResponse>> getAllReports() {
        return ResponseEntity.ok(reportingService.getAllReports());
    }

    // ── GET /api/v1/reports/scope?scope=REQUEST ───────────────────────────────
    @Operation(summary = "Get reports filtered by scope")
    @PreAuthorize("hasAnyRole('DEPARTMENT_HEAD','CITY_ADMINISTRATOR','COMPLIANCE_OFFICER')")
    @GetMapping("/scope")
    public ResponseEntity<List<ReportResponse>> getReportsByScope(
            @RequestParam ReportScope scope) {
        return ResponseEntity.ok(reportingService.getReportsByScope(scope));
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    private Long extractUserId(Authentication auth) {
        return ((Number) ((UsernamePasswordAuthenticationToken) auth).getDetails()).longValue();
    }
}
