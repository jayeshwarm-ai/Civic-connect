package com.civicconnect.citizen.controller;

import com.civicconnect.citizen.dto.response.CitizenValidationResponse;
import com.civicconnect.citizen.entity.Citizen;
import com.civicconnect.citizen.repository.CitizenRepository;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Internal-only API consumed by other microservices via Feign clients.
 *
 * NOT exposed in Swagger (@Hidden).
 * NOT behind JWT auth (permitted in SecurityConfig via /internal/**).
 * Only reachable service-to-service inside the cluster.
 *
 * Used by:
 *  - service-request-service  → validate citizenId before submitting request
 *  - feedback-service         → validate citizenId before submitting feedback
 */
@Hidden
@RestController
@RequestMapping("/internal/citizens")
@RequiredArgsConstructor
public class CitizenValidationController {

    private final CitizenRepository citizenRepository;

    /**
     * Returns citizen details including account status.
     * Downstream services check accountStatus == ACTIVE before allowing operations.
     */
    @GetMapping("/{citizenId}")
    public ResponseEntity<CitizenValidationResponse> validateCitizen(
            @PathVariable Long citizenId) {
        return citizenRepository.findById(citizenId)
                .map(c -> ResponseEntity.ok(mapToValidation(c, true)))
                .orElseGet(() -> ResponseEntity.ok(
                        CitizenValidationResponse.builder()
                                .citizenId(citizenId)
                                .exists(false)
                                .build()));
    }

    /**
     * Lightweight existence check.
     */
    @GetMapping("/{citizenId}/exists")
    public ResponseEntity<Boolean> existsById(@PathVariable Long citizenId) {
        return ResponseEntity.ok(citizenRepository.existsById(citizenId));
    }

    /**
     * Lookup by userId — used by service-request-service to find citizenId
     * from the JWT userId claim.
     */
    @GetMapping("/by-user/{userId}")
    public ResponseEntity<CitizenValidationResponse> getByUserId(@PathVariable Long userId) {
        return citizenRepository.findByUserId(userId)
                .map(c -> ResponseEntity.ok(mapToValidation(c, true)))
                .orElseGet(() -> ResponseEntity.ok(
                        CitizenValidationResponse.builder()
                                .userId(userId)
                                .exists(false)
                                .build()));
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    private CitizenValidationResponse mapToValidation(Citizen c, boolean exists) {
        return CitizenValidationResponse.builder()
                .citizenId(c.getCitizenId())
                .userId(c.getUserId())
                .name(c.getName())
                .email(c.getEmail())
                .accountStatus(c.getAccountStatus())
                .exists(exists)
                .build();
    }
}
