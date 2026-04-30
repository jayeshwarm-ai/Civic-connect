package com.civicconnect.identity.controller;

import com.civicconnect.identity.dto.request.CreateStaffRequest;
import com.civicconnect.identity.dto.response.StaffResponse;
import com.civicconnect.identity.enums.Role;
import com.civicconnect.identity.enums.UserStatus;
import com.civicconnect.identity.service.UserService;
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
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Staff Management", description = "CITY_ADMINISTRATOR creates and manages staff accounts")
@SecurityRequirement(name = "BearerAuth")
public class UserController {

    private final UserService userService;

    // ── POST /api/v1/users/staff ──────────────────────────────────────────────
    @Operation(
        summary = "Create staff account — CITY_ADMINISTRATOR only",
        description = "Creates SERVICE_OFFICER, DEPARTMENT_HEAD, CITY_ADMINISTRATOR, " +
                      "or COMPLIANCE_OFFICER accounts. CITIZEN role is not allowed here."
    )
    @ApiResponse(responseCode = "201", description = "Staff account created")
    @ApiResponse(responseCode = "400", description = "CITIZEN role not allowed")
    @ApiResponse(responseCode = "409", description = "Email or phone already registered")
    @PreAuthorize("hasRole('CITY_ADMINISTRATOR')")
    @PostMapping("/staff")
    public ResponseEntity<StaffResponse> createStaff(
            @Valid @RequestBody CreateStaffRequest request,
            Authentication authentication) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.createStaff(request, extractUserId(authentication)));
    }

    // ── GET /api/v1/users/staff?role=SERVICE_OFFICER ──────────────────────────
    @Operation(
        summary = "Get staff by role — CITY_ADMINISTRATOR only",
        description = "Filter staff by role. Querying CITIZEN role is not allowed here."
    )
    @ApiResponse(responseCode = "200", description = "Staff list returned")
    @PreAuthorize("hasRole('CITY_ADMINISTRATOR')")
    @GetMapping("/staff")
    public ResponseEntity<List<StaffResponse>> getStaffByRole(
            @Parameter(description = "Role to filter — not CITIZEN")
            @RequestParam Role role) {
        return ResponseEntity.ok(userService.getStaffByRole(role));
    }

    // ── GET /api/v1/users/staff/{userId} ──────────────────────────────────────
    @Operation(summary = "Get staff by ID — CITY_ADMINISTRATOR only")
    @ApiResponse(responseCode = "200", description = "Staff found")
    @ApiResponse(responseCode = "404", description = "User not found")
    @PreAuthorize("hasRole('CITY_ADMINISTRATOR')")
    @GetMapping("/staff/{userId}")
    public ResponseEntity<StaffResponse> getStaffById(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getStaffById(userId));
    }

    // ── PATCH /api/v1/users/staff/{userId}/status?newStatus=SUSPENDED ─────────
    @Operation(
        summary = "Update staff status — CITY_ADMINISTRATOR only",
        description = "Set status to ACTIVE or SUSPENDED for a staff account."
    )
    @ApiResponse(responseCode = "200", description = "Status updated")
    @ApiResponse(responseCode = "400", description = "Status already set to requested value")
    @ApiResponse(responseCode = "404", description = "User not found")
    @PreAuthorize("hasRole('CITY_ADMINISTRATOR')")
    @PatchMapping("/staff/{userId}/status")
    public ResponseEntity<StaffResponse> updateStaffStatus(
            @PathVariable Long userId,
            @Parameter(description = "New status: ACTIVE or SUSPENDED")
            @RequestParam UserStatus newStatus,
            Authentication authentication) {
        return ResponseEntity.ok(
                userService.updateStaffStatus(userId, newStatus, extractUserId(authentication)));
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    private Long extractUserId(Authentication auth) {
        return ((Number) ((UsernamePasswordAuthenticationToken) auth).getDetails()).longValue();
    }
}
