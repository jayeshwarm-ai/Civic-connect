package com.civicconnect.feedback.controller;

import com.civicconnect.feedback.entity.SatisfactionMetric;
import com.civicconnect.feedback.repository.FeedbackRepository;
import com.civicconnect.feedback.repository.SatisfactionMetricRepository;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Internal-only stats endpoint for reporting-service.
 * NOT in Swagger. NOT behind JWT. Internal cluster access only.
 */
@Hidden
@RestController
@RequestMapping("/internal/feedback/stats")
@RequiredArgsConstructor
public class FeedbackStatsController {

    private final FeedbackRepository          feedbackRepository;
    private final SatisfactionMetricRepository satisfactionMetricRepository;

    @GetMapping
    public ResponseEntity<FeedbackStatsResponse> getStats() {
        long total = feedbackRepository.count();

        double avgRating = feedbackRepository.findAll().stream()
                .mapToInt(f -> f.getRating()).average().orElse(0.0);

        List<SatisfactionMetric> metrics =
                satisfactionMetricRepository.findAllByOrderByAverageScoreDesc();

        long officersRated = metrics.size();
        String topOfficerName  = metrics.isEmpty() ? "N/A" : metrics.get(0).getOfficerName();
        double topOfficerScore = metrics.isEmpty() ? 0.0  : metrics.get(0).getAverageScore();

        return ResponseEntity.ok(FeedbackStatsResponse.builder()
                .totalFeedbacks(total)
                .averageRating(avgRating)
                .officersRated(officersRated)
                .topOfficerName(topOfficerName)
                .topOfficerScore(topOfficerScore)
                .build());
    }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class FeedbackStatsResponse {
        private long   totalFeedbacks;
        private double averageRating;
        private long   officersRated;
        private String topOfficerName;
        private double topOfficerScore;
    }
}
