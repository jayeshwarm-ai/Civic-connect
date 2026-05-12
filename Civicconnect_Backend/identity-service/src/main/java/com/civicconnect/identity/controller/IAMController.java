package com.civicconnect.identity.controller;

import com.civicconnect.identity.dto.response.AuditLogResponse;
import com.civicconnect.identity.dto.response.UserResponse;
import com.civicconnect.identity.enums.AuditAction;
import com.civicconnect.identity.service.IAMService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/iam")
@RequiredArgsConstructor
@Tag(name = "IAM", description = "Profile & Audit log management — JWT required")
@SecurityRequirement(name = "BearerAuth")
public class IAMController {

    private final IAMService iamService;

    // ── All authenticated roles ───────────────────────────────────────────────

    @Operation(summary = "Get my profile", description = "Returns the profile of the authenticated user.")
    @ApiResponse(responseCode = "200", description = "Profile returned")
    @PreAuthorize("hasAnyRole('CITIZEN','SERVICE_OFFICER','DEPARTMENT_HEAD','CITY_ADMINISTRATOR','COMPLIANCE_OFFICER')")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(Authentication authentication) {
        return ResponseEntity.ok(iamService.getMyProfile(extractUserId(authentication)));
    }

    // ── CITY_ADMINISTRATOR only ───────────────────────────────────────────────

    @Operation(summary = "Get all audit logs — CITY_ADMINISTRATOR only")
    @ApiResponse(responseCode = "200", description = "Audit logs returned")
    @PreAuthorize("hasRole('CITY_ADMINISTRATOR')")
    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLogResponse>> getAllAuditLogs() {
        return ResponseEntity.ok(iamService.getAllAuditLogs());
    }

    @Operation(summary = "Get audit logs by resource — CITY_ADMINISTRATOR only")
    @PreAuthorize("hasRole('CITY_ADMINISTRATOR')")
    @GetMapping("/audit-logs/resource")
    public ResponseEntity<List<AuditLogResponse>> getByResource(
            @Parameter(description = "e.g. USER, CITIZEN, SERVICE_REQUEST")
            @RequestParam String resource,
            @RequestParam String resourceId) {
        return ResponseEntity.ok(iamService.getAuditLogsByResource(resource, resourceId));
    }

    @Operation(summary = "Get audit logs by action type — CITY_ADMINISTRATOR only")
    @PreAuthorize("hasRole('CITY_ADMINISTRATOR')")
    @GetMapping("/audit-logs/action")
    public ResponseEntity<List<AuditLogResponse>> getByAction(
            @RequestParam AuditAction action) {
        return ResponseEntity.ok(iamService.getAuditLogsByAction(action));
    }

    @Operation(summary = "Deactivate a user — CITY_ADMINISTRATOR only")
    @ApiResponse(responseCode = "200", description = "User deactivated")
    @ApiResponse(responseCode = "400", description = "Already INACTIVE")
    @PreAuthorize("hasRole('CITY_ADMINISTRATOR')")
    @PatchMapping("/users/{userId}/deactivate")
    public ResponseEntity<UserResponse> deactivateUser(
            @PathVariable Long userId, Authentication authentication) {
        return ResponseEntity.ok(
                iamService.deactivateUser(userId, extractUserId(authentication)));
    }

    // ── CITY_ADMINISTRATOR + COMPLIANCE_OFFICER ───────────────────────────────

    @Operation(summary = "Get audit logs by user — ADMIN + COMPLIANCE_OFFICER")
    @PreAuthorize("hasAnyRole('CITY_ADMINISTRATOR','COMPLIANCE_OFFICER')")
    @GetMapping("/audit-logs/user/{userId}")
    public ResponseEntity<List<AuditLogResponse>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(iamService.getAuditLogsByUser(userId));
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    private Long extractUserId(Authentication auth) {
        return ((Number) ((UsernamePasswordAuthenticationToken) auth).getDetails()).longValue();
    }
}
