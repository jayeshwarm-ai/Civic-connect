package com.civicconnect.resolution.controller;

import com.civicconnect.resolution.dto.response.ResolutionValidationResponse;
import com.civicconnect.resolution.repository.ResolutionRepository;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Internal-only API consumed by compliance-service via Feign.
 *
 * NOT in Swagger (@Hidden). NOT behind JWT. Accessible service-to-service only.
 *
 * Used by:
 *  - compliance-service → verify resolution exists before creating a compliance record
 */
@Hidden
@RestController
@RequestMapping("/internal/resolutions")
@RequiredArgsConstructor
public class ResolutionValidationController {

    private final ResolutionRepository resolutionRepository;

    /**
     * Returns resolution validation data.
     * compliance-service calls this before creating a RESOLUTION-type compliance record.
     */
    @GetMapping("/{resolutionId}")
    public ResponseEntity<ResolutionValidationResponse> getResolution(
            @PathVariable Long resolutionId) {
        return resolutionRepository.findById(resolutionId)
                .map(r -> ResponseEntity.ok(ResolutionValidationResponse.builder()
                        .resolutionId(r.getResolutionId())
                        .requestId(r.getRequestId())
                        .officerUserId(r.getOfficerUserId())
                        .status(r.getStatus().name())
                        .exists(true)
                        .build()))
                .orElseGet(() -> ResponseEntity.ok(
                        ResolutionValidationResponse.builder()
                                .resolutionId(resolutionId)
                                .exists(false)
                                .build()));
    }
}
