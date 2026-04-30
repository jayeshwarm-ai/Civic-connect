package com.civicconnect.feedback.service;

import com.civicconnect.feedback.dto.request.SubmitFeedbackRequest;
import com.civicconnect.feedback.dto.response.FeedbackResponse;
import com.civicconnect.feedback.dto.response.SatisfactionMetricResponse;

import java.util.List;

/**
 * Service interface for Feedback operations.
 */
public interface FeedbackService {
    FeedbackResponse submitFeedback(Long citizenUserId, SubmitFeedbackRequest request);
    FeedbackResponse getFeedbackByRequestId(Long requestId);
    List<FeedbackResponse> getFeedbacksByCitizenId(Long citizenId);
    SatisfactionMetricResponse getSatisfactionMetricByOfficerId(Long officerUserId);
    List<SatisfactionMetricResponse> getAllSatisfactionMetrics();
}
