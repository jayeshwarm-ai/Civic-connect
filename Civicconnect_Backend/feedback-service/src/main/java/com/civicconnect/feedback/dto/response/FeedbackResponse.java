package com.civicconnect.feedback.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FeedbackResponse {

    private Long          feedbackId;
    private Long          requestId;
    private Long          citizenId;
    private String        citizenName;
    private Integer       rating;
    private String        comments;
    private LocalDateTime createdAt;
}
