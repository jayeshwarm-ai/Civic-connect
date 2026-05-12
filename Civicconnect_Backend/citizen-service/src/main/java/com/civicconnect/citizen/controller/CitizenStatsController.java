package com.civicconnect.citizen.controller;

import com.civicconnect.citizen.repository.CitizenRepository;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Internal-only stats endpoint for reporting-service.
 * NOT in Swagger. NOT behind JWT. Internal cluster access only.
 */
@Hidden
@RestController
@RequestMapping("/internal/citizens/stats")
@RequiredArgsConstructor
public class CitizenStatsController {

    private final CitizenRepository citizenRepository;

    @GetMapping
    public ResponseEntity<CitizenStatsResponse> getStats() {
        return ResponseEntity.ok(CitizenStatsResponse.builder()
                .totalCitizens(citizenRepository.count())
                .build());
    }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CitizenStatsResponse {
        private long totalCitizens;
    }
}
