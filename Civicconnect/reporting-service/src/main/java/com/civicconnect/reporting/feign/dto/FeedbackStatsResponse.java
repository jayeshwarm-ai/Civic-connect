package com.civicconnect.reporting.feign.dto;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FeedbackStatsResponse {
    private long totalFeedbacks, officersRated;
    private double averageRating, topOfficerScore;
    private String topOfficerName;
}
