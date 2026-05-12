package com.civicconnect.feedback.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SatisfactionMetricResponse {

    private Long          metricId;
    private Long          officerUserId;
    private String        officerName;
    private Long          totalRatingSum;
    private Long          totalFeedbackCount;
    private Double        averageScore;
    private LocalDateTime lastUpdatedAt;
}
