package com.civicconnect.identity.controller;

import com.civicconnect.identity.dto.request.LoginRequest;
import com.civicconnect.identity.dto.request.ResetPasswordRequest;
import com.civicconnect.identity.dto.request.CreateStaffRequest;
import com.civicconnect.identity.dto.response.LoginResponse;
import com.civicconnect.identity.dto.response.StaffResponse;
import com.civicconnect.identity.service.AuthService;
import com.civicconnect.identity.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login for all roles — no token required")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @Operation(
        summary = "Login",
        description = "Authenticate using email + password. Returns a signed JWT (valid 1 hour). " +
                      "Pass it on all subsequent requests as:  Authorization: Bearer <token>. " +
                      "Works for all roles: CITIZEN, SERVICE_OFFICER, DEPARTMENT_HEAD, " +
                      "CITY_ADMINISTRATOR, COMPLIANCE_OFFICER."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login successful — JWT returned",
                content = @Content(schema = @Schema(implementation = LoginResponse.class))),
        @ApiResponse(responseCode = "400", description = "Account SUSPENDED"),
        @ApiResponse(responseCode = "404", description = "No account with that email")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(
        summary = "Reset password",
        description = "Reset password using email + phone verification. No token required."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Password reset successful"),
        @ApiResponse(responseCode = "400", description = "Phone does not match"),
        @ApiResponse(responseCode = "404", description = "No account with that email")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<java.util.Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(java.util.Map.of("message", "Password reset successful. Please login with your new password."));
    }

    @Operation(
        summary = "Register staff (public)",
        description = "Public registration for non-CITIZEN roles. Creates SERVICE_OFFICER, DEPARTMENT_HEAD, CITY_ADMINISTRATOR, or COMPLIANCE_OFFICER."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Staff registered"),
        @ApiResponse(responseCode = "400", description = "CITIZEN role not allowed"),
        @ApiResponse(responseCode = "409", description = "Email or phone already registered")
    })
    @PostMapping("/register-staff")
    public ResponseEntity<StaffResponse> registerStaff(@Valid @RequestBody CreateStaffRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerStaffPublic(request));
    }
}
