package com.civicconnect.feedback.service.impl;

import com.civicconnect.feedback.dto.request.SubmitFeedbackRequest;
import com.civicconnect.feedback.dto.response.FeedbackResponse;
import com.civicconnect.feedback.dto.response.SatisfactionMetricResponse;
import com.civicconnect.feedback.entity.Feedback;
import com.civicconnect.feedback.entity.SatisfactionMetric;
import com.civicconnect.feedback.enums.NotificationCategory;
import com.civicconnect.feedback.exception.InvalidOperationException;
import com.civicconnect.feedback.exception.ResourceNotFoundException;
import com.civicconnect.feedback.feign.*;
import com.civicconnect.feedback.feign.dto.*;
import com.civicconnect.feedback.repository.FeedbackRepository;
import com.civicconnect.feedback.repository.SatisfactionMetricRepository;
import com.civicconnect.feedback.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository          feedbackRepository;
    private final SatisfactionMetricRepository satisfactionMetricRepository;
    private final ServiceRequestFeignClient   serviceRequestFeignClient;
    private final CitizenFeignClient          citizenFeignClient;
    private final IdentityFeignClient         identityFeignClient;
    private final NotificationFeignClient     notificationFeignClient;

    @Override
    @Transactional
    public FeedbackResponse submitFeedback(Long citizenUserId, SubmitFeedbackRequest request) {
        CitizenValidationResponse citizen = citizenFeignClient.getCitizenByUserId(citizenUserId);
        if (!citizen.isExists()) {
            throw new ResourceNotFoundException("Citizen profile not found for userId: " + citizenUserId);
        }

        ServiceRequestValidationResponse sr = serviceRequestFeignClient.getRequest(request.getRequestId());
        if (!sr.isExists()) {
            throw new ResourceNotFoundException("ServiceRequest not found with id: " + request.getRequestId());
        }
        if (!"CLOSED".equals(sr.getStatus())) {
            throw new InvalidOperationException("Feedback can only be submitted for CLOSED requests. Current status: " + sr.getStatus());
        }
        if (!citizen.getCitizenId().equals(sr.getCitizenId())) {
            throw new InvalidOperationException("You can only submit feedback for your own service requests.");
        }
        if (feedbackRepository.existsByRequestId(request.getRequestId())) {
            throw new InvalidOperationException("Feedback has already been submitted for requestId: " + request.getRequestId());
        }

        Feedback feedback = Feedback.builder()
                .requestId(request.getRequestId())
                .citizenId(citizen.getCitizenId())
                .citizenName(citizen.getName())
                .citizenUserId(citizenUserId)
                .officerUserId(sr.getAssignedOfficerUserId())
                .rating(request.getRating())
                .comments(request.getComments())
                .build();

        feedback = feedbackRepository.save(feedback);

        writeAuditLog(citizenUserId, "FEEDBACK_SUBMITTED", "FEEDBACK",
                String.valueOf(feedback.getFeedbackId()),
                "Rating: " + request.getRating() + " for requestId: " + request.getRequestId());

        if (sr.getAssignedOfficerUserId() != null) {
            sendNotification(sr.getAssignedOfficerUserId(), request.getRequestId(),
                    "Citizen submitted feedback for request #" + request.getRequestId() + ". Rating: " + request.getRating() + "/5.",
                    NotificationCategory.FEEDBACK);
            updateSatisfactionMetric(sr.getAssignedOfficerUserId(), request.getRating(), citizenUserId);
        }

        return mapToFeedbackResponse(feedback);
    }

    @Override
    @Transactional(readOnly = true)
    public FeedbackResponse getFeedbackByRequestId(Long requestId) {
        Feedback feedback = feedbackRepository.findByRequestId(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback not found for requestId: " + requestId));
        return mapToFeedbackResponse(feedback);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedbackResponse> getFeedbacksByCitizenId(Long citizenId) {
        CitizenValidationResponse citizen = citizenFeignClient.validateCitizen(citizenId);
        if (!citizen.isExists()) {
            throw new ResourceNotFoundException("Citizen", citizenId);
        }
        return feedbackRepository.findByCitizenIdOrderByCreatedAtDesc(citizenId)
                .stream().map(this::mapToFeedbackResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SatisfactionMetricResponse getSatisfactionMetricByOfficerId(Long officerUserId) {
        SatisfactionMetric metric = satisfactionMetricRepository.findByOfficerUserId(officerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("No satisfaction metric found for officerId: " + officerUserId));
        return mapToMetricResponse(metric);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SatisfactionMetricResponse> getAllSatisfactionMetrics() {
        return satisfactionMetricRepository.findAllByOrderByAverageScoreDesc()
                .stream().map(this::mapToMetricResponse).collect(Collectors.toList());
    }

    private void updateSatisfactionMetric(Long officerUserId, Integer rating, Long performedBy) {
        SatisfactionMetric metric;
        if (satisfactionMetricRepository.existsByOfficerUserId(officerUserId)) {
            metric = satisfactionMetricRepository.findByOfficerUserId(officerUserId).get();
            metric.setTotalRatingSum(metric.getTotalRatingSum() + rating);
            metric.setTotalFeedbackCount(metric.getTotalFeedbackCount() + 1);
        } else {
            UserValidationResponse officer = identityFeignClient.validateUser(officerUserId);
            String officerName = officer.isExists() ? officer.getName() : "Officer#" + officerUserId;
            metric = SatisfactionMetric.builder()
                    .officerUserId(officerUserId)
                    .officerName(officerName)
                    .totalRatingSum((long) rating)
                    .totalFeedbackCount(1L)
                    .averageScore(0.0)
                    .build();
        }
        metric.setAverageScore((double) metric.getTotalRatingSum() / metric.getTotalFeedbackCount());
        satisfactionMetricRepository.save(metric);

        writeAuditLog(performedBy, "SATISFACTION_METRIC_UPDATED", "SATISFACTION_METRIC",
                String.valueOf(officerUserId),
                "Officer satisfaction updated. New average: " + String.format("%.2f", metric.getAverageScore()));
    }

    private void writeAuditLog(Long performedBy, String action, String resource, String resourceId, String detail) {
        try {
            identityFeignClient.writeAuditLog(AuditLogRequest.builder()
                    .performedBy(performedBy).action(action).resource(resource).resourceId(resourceId).detail(detail)
                    .build());
        } catch (Exception e) {
            log.warn("Failed to write audit log: action={}: {}", action, e.getMessage());
        }
    }

    private void sendNotification(Long userId, Long requestId, String message, NotificationCategory category) {
        try {
            notificationFeignClient.sendNotification(SendNotificationRequest.builder()
                    .userId(userId).requestId(requestId).message(message).category(category.name())
                    .build());
        } catch (Exception e) {
            log.warn("Failed to send notification to userId={}: {}", userId, e.getMessage());
        }
    }

    private FeedbackResponse mapToFeedbackResponse(Feedback f) {
        return FeedbackResponse.builder()
                .feedbackId(f.getFeedbackId())
                .requestId(f.getRequestId())
                .citizenId(f.getCitizenId())
                .citizenName(f.getCitizenName())
                .rating(f.getRating())
                .comments(f.getComments())
                .createdAt(f.getCreatedAt())
                .build();
    }

    private SatisfactionMetricResponse mapToMetricResponse(SatisfactionMetric m) {
        return SatisfactionMetricResponse.builder()
                .metricId(m.getMetricId())
                .officerUserId(m.getOfficerUserId())
                .officerName(m.getOfficerName())
                .totalRatingSum(m.getTotalRatingSum())
                .totalFeedbackCount(m.getTotalFeedbackCount())
                .averageScore(m.getAverageScore())
                .lastUpdatedAt(m.getLastUpdatedAt())
                .build();
    }
}

