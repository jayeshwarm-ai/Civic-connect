package com.civicconnect.feedback;

import com.civicconnect.feedback.dto.request.SubmitFeedbackRequest;
import com.civicconnect.feedback.dto.response.FeedbackResponse;
import com.civicconnect.feedback.dto.response.SatisfactionMetricResponse;
import com.civicconnect.feedback.entity.Feedback;
import com.civicconnect.feedback.entity.SatisfactionMetric;
import com.civicconnect.feedback.exception.InvalidOperationException;
import com.civicconnect.feedback.exception.ResourceNotFoundException;
import com.civicconnect.feedback.feign.*;
import com.civicconnect.feedback.feign.dto.*;
import com.civicconnect.feedback.repository.FeedbackRepository;
import com.civicconnect.feedback.repository.SatisfactionMetricRepository;
import com.civicconnect.feedback.service.FeedbackService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedbackService Tests")
class FeedbackServiceTest {

    @Mock private FeedbackRepository           feedbackRepository;
    @Mock private SatisfactionMetricRepository satisfactionMetricRepository;
    @Mock private ServiceRequestFeignClient    serviceRequestFeignClient;
    @Mock private CitizenFeignClient           citizenFeignClient;
    @Mock private IdentityFeignClient          identityFeignClient;
    @Mock private NotificationFeignClient      notificationFeignClient;

    @InjectMocks
    private FeedbackService feedbackService;

    private CitizenValidationResponse activeCitizen;
    private ServiceRequestValidationResponse closedRequest;
    private Feedback savedFeedback;

    @BeforeEach
    void setUp() {
        activeCitizen = CitizenValidationResponse.builder()
                .citizenId(10L).userId(100L)
                .name("Alice Citizen").email("alice@civic.com")
                .accountStatus("ACTIVE").exists(true).build();

        closedRequest = ServiceRequestValidationResponse.builder()
                .requestId(20L).citizenId(10L).citizenUserId(100L)
                .assignedOfficerUserId(200L).status("CLOSED").exists(true).build();

        savedFeedback = Feedback.builder()
                .feedbackId(1L).requestId(20L)
                .citizenId(10L).citizenName("Alice Citizen")
                .citizenUserId(100L).officerUserId(200L)
                .rating(5).comments("Great service!")
                .createdAt(LocalDateTime.now()).build();
    }

    // ── submitFeedback ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should submit feedback successfully")
    void shouldSubmitFeedbackSuccessfully() {
        SubmitFeedbackRequest req = buildRequest(20L, 5, "Great service!");

        when(citizenFeignClient.getCitizenByUserId(100L)).thenReturn(activeCitizen);
        when(serviceRequestFeignClient.getRequest(20L)).thenReturn(closedRequest);
        when(feedbackRepository.existsByRequestId(20L)).thenReturn(false);
        when(feedbackRepository.save(any())).thenReturn(savedFeedback);
        when(satisfactionMetricRepository.existsByOfficerUserId(200L)).thenReturn(false);
        when(identityFeignClient.validateUser(200L))
                .thenReturn(UserValidationResponse.builder()
                        .userId(200L).name("Officer Bob").exists(true).build());
        when(satisfactionMetricRepository.save(any())).thenReturn(mock(SatisfactionMetric.class));
        doNothing().when(identityFeignClient).writeAuditLog(any());
        doNothing().when(notificationFeignClient).sendNotification(any());

        FeedbackResponse response = feedbackService.submitFeedback(100L, req);

        assertThat(response.getFeedbackId()).isEqualTo(1L);
        assertThat(response.getRating()).isEqualTo(5);
        assertThat(response.getCitizenName()).isEqualTo("Alice Citizen");
        verify(feedbackRepository).save(any(Feedback.class));
        verify(satisfactionMetricRepository).save(any(SatisfactionMetric.class));
        verify(notificationFeignClient).sendNotification(any(SendNotificationRequest.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when citizen not found")
    void shouldThrowWhenCitizenNotFound() {
        when(citizenFeignClient.getCitizenByUserId(999L))
                .thenReturn(CitizenValidationResponse.builder().userId(999L).exists(false).build());

        assertThatThrownBy(() ->
                feedbackService.submitFeedback(999L, buildRequest(20L, 4, null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Citizen profile not found");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when request not found")
    void shouldThrowWhenRequestNotFound() {
        when(citizenFeignClient.getCitizenByUserId(100L)).thenReturn(activeCitizen);
        when(serviceRequestFeignClient.getRequest(99L))
                .thenReturn(ServiceRequestValidationResponse.builder()
                        .requestId(99L).exists(false).build());

        assertThatThrownBy(() ->
                feedbackService.submitFeedback(100L, buildRequest(99L, 3, null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ServiceRequest not found");
    }

    @Test
    @DisplayName("Should throw InvalidOperationException when request is not CLOSED")
    void shouldThrowWhenRequestNotClosed() {
        ServiceRequestValidationResponse resolvedRequest =
                ServiceRequestValidationResponse.builder()
                        .requestId(20L).citizenId(10L).citizenUserId(100L)
                        .status("RESOLVED").exists(true).build();

        when(citizenFeignClient.getCitizenByUserId(100L)).thenReturn(activeCitizen);
        when(serviceRequestFeignClient.getRequest(20L)).thenReturn(resolvedRequest);

        assertThatThrownBy(() ->
                feedbackService.submitFeedback(100L, buildRequest(20L, 4, null)))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("CLOSED requests");
    }

    @Test
    @DisplayName("Should throw InvalidOperationException when citizen is not the request owner")
    void shouldThrowWhenNotRequestOwner() {
        // closedRequest.citizenId=10, but activeCitizen.citizenId is also 10 by default
        // Change the request to belong to a different citizen (50L)
        ServiceRequestValidationResponse otherCitizensRequest =
                ServiceRequestValidationResponse.builder()
                        .requestId(20L).citizenId(50L).citizenUserId(500L)
                        .status("CLOSED").exists(true).build();

        when(citizenFeignClient.getCitizenByUserId(100L)).thenReturn(activeCitizen);
        when(serviceRequestFeignClient.getRequest(20L)).thenReturn(otherCitizensRequest);

        assertThatThrownBy(() ->
                feedbackService.submitFeedback(100L, buildRequest(20L, 3, null)))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("your own service requests");
    }

    @Test
    @DisplayName("Should throw InvalidOperationException when feedback already submitted")
    void shouldThrowWhenFeedbackAlreadySubmitted() {
        when(citizenFeignClient.getCitizenByUserId(100L)).thenReturn(activeCitizen);
        when(serviceRequestFeignClient.getRequest(20L)).thenReturn(closedRequest);
        when(feedbackRepository.existsByRequestId(20L)).thenReturn(true);

        assertThatThrownBy(() ->
                feedbackService.submitFeedback(100L, buildRequest(20L, 5, null)))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("already been submitted");
    }

    @Test
    @DisplayName("Should update existing satisfaction metric when officer already has one")
    void shouldUpdateExistingSatisfactionMetric() {
        SatisfactionMetric existing = SatisfactionMetric.builder()
                .metricId(1L).officerUserId(200L).officerName("Officer Bob")
                .totalRatingSum(15L).totalFeedbackCount(3L).averageScore(5.0)
                .build();

        when(citizenFeignClient.getCitizenByUserId(100L)).thenReturn(activeCitizen);
        when(serviceRequestFeignClient.getRequest(20L)).thenReturn(closedRequest);
        when(feedbackRepository.existsByRequestId(20L)).thenReturn(false);
        when(feedbackRepository.save(any())).thenReturn(savedFeedback);
        when(satisfactionMetricRepository.existsByOfficerUserId(200L)).thenReturn(true);
        when(satisfactionMetricRepository.findByOfficerUserId(200L)).thenReturn(Optional.of(existing));
        when(satisfactionMetricRepository.save(any())).thenReturn(existing);
        doNothing().when(identityFeignClient).writeAuditLog(any());
        doNothing().when(notificationFeignClient).sendNotification(any());

        feedbackService.submitFeedback(100L, buildRequest(20L, 5, null));

        // totalRatingSum should be 15 + 5 = 20, count 3 + 1 = 4
        verify(satisfactionMetricRepository).save(argThat(m ->
                m.getTotalRatingSum() == 20L && m.getTotalFeedbackCount() == 4L));
    }

    // ── getFeedbackByRequestId ─────────────────────────────────────────────────

    @Test
    @DisplayName("Should return feedback by request ID")
    void shouldReturnFeedbackByRequestId() {
        when(feedbackRepository.findByRequestId(20L)).thenReturn(Optional.of(savedFeedback));

        FeedbackResponse response = feedbackService.getFeedbackByRequestId(20L);

        assertThat(response.getRequestId()).isEqualTo(20L);
        assertThat(response.getRating()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when no feedback for request")
    void shouldThrowWhenNoFeedback() {
        when(feedbackRepository.findByRequestId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> feedbackService.getFeedbackByRequestId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Feedback not found for requestId");
    }

    // ── getSatisfactionMetricByOfficerId ───────────────────────────────────────

    @Test
    @DisplayName("Should return satisfaction metric for officer")
    void shouldReturnSatisfactionMetricForOfficer() {
        SatisfactionMetric metric = SatisfactionMetric.builder()
                .metricId(1L).officerUserId(200L).officerName("Officer Bob")
                .totalRatingSum(20L).totalFeedbackCount(4L).averageScore(5.0)
                .lastUpdatedAt(LocalDateTime.now()).build();

        when(satisfactionMetricRepository.findByOfficerUserId(200L))
                .thenReturn(Optional.of(metric));

        SatisfactionMetricResponse response =
                feedbackService.getSatisfactionMetricByOfficerId(200L);

        assertThat(response.getOfficerUserId()).isEqualTo(200L);
        assertThat(response.getAverageScore()).isEqualTo(5.0);
        assertThat(response.getTotalFeedbackCount()).isEqualTo(4L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when no metric for officer")
    void shouldThrowWhenNoMetricForOfficer() {
        when(satisfactionMetricRepository.findByOfficerUserId(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                feedbackService.getSatisfactionMetricByOfficerId(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No satisfaction metric found for officerId");
    }

    // ── getAllSatisfactionMetrics ───────────────────────────────────────────────

    @Test
    @DisplayName("Should return all metrics ordered by score descending")
    void shouldReturnAllMetricsOrdered() {
        SatisfactionMetric m1 = SatisfactionMetric.builder()
                .metricId(1L).officerUserId(200L).officerName("Top Officer")
                .totalRatingSum(25L).totalFeedbackCount(5L).averageScore(5.0)
                .lastUpdatedAt(LocalDateTime.now()).build();

        SatisfactionMetric m2 = SatisfactionMetric.builder()
                .metricId(2L).officerUserId(201L).officerName("Second Officer")
                .totalRatingSum(16L).totalFeedbackCount(4L).averageScore(4.0)
                .lastUpdatedAt(LocalDateTime.now()).build();

        when(satisfactionMetricRepository.findAllByOrderByAverageScoreDesc())
                .thenReturn(List.of(m1, m2));

        List<SatisfactionMetricResponse> result =
                feedbackService.getAllSatisfactionMetrics();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getAverageScore()).isEqualTo(5.0);
        assertThat(result.get(1).getAverageScore()).isEqualTo(4.0);
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    private SubmitFeedbackRequest buildRequest(Long requestId, int rating, String comments) {
        SubmitFeedbackRequest req = new SubmitFeedbackRequest();
        req.setRequestId(requestId);
        req.setRating(rating);
        req.setComments(comments);
        return req;
    }
}
