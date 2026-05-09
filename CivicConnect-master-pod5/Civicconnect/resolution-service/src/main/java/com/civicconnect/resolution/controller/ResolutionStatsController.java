package com.civicconnect.resolution.controller;

import com.civicconnect.resolution.enums.ResolutionStatus;
import com.civicconnect.resolution.repository.ResolutionRepository;
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
@RequestMapping("/internal/resolutions/stats")
@RequiredArgsConstructor
public class ResolutionStatsController {

    private final ResolutionRepository resolutionRepository;

    @GetMapping
    public ResponseEntity<ResolutionStatsResponse> getStats() {
        return ResponseEntity.ok(ResolutionStatsResponse.builder()
                .total(resolutionRepository.count())
                .inProgress(resolutionRepository.countByStatus(ResolutionStatus.IN_PROGRESS))
                .completed(resolutionRepository.countByStatus(ResolutionStatus.COMPLETED))
                .build());
    }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ResolutionStatsResponse {
        private long total;
        private long inProgress;
        private long completed;
    }
}
