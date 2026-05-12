package com.civicconnect.feedback.controller;

import com.civicconnect.feedback.dto.request.SubmitFeedbackRequest;
import com.civicconnect.feedback.dto.response.FeedbackResponse;
import com.civicconnect.feedback.dto.response.SatisfactionMetricResponse;
import com.civicconnect.feedback.service.FeedbackService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedbackController Tests")
class FeedbackControllerTest {

    @Mock private FeedbackService feedbackService;

    @InjectMocks
    private FeedbackController controller;

    private UsernamePasswordAuthenticationToken authWithUserId(Long userId) {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("user", null, Collections.emptyList());
        auth.setDetails(userId);
        return auth;
    }

    private FeedbackResponse sampleFeedback;
    private SatisfactionMetricResponse sampleMetric;

    @BeforeEach
    void setUp() {
        sampleFeedback = FeedbackResponse.builder()
                .feedbackId(1L)
                .requestId(10L)
                .citizenId(50L)
                .rating(5)
                .build();
        sampleMetric = SatisfactionMetricResponse.builder()
                .officerUserId(200L)
                .officerName("Officer Bob")
                .averageScore(4.5)
                .build();
    }

    @Test
    @DisplayName("submitFeedback → 201")
    void submitFeedback_returns201() {
        SubmitFeedbackRequest req = new SubmitFeedbackRequest();
        when(feedbackService.submitFeedback(eq(100L), eq(req))).thenReturn(sampleFeedback);

        ResponseEntity<FeedbackResponse> resp =
                controller.submitFeedback(req, authWithUserId(100L));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(feedbackService).submitFeedback(eq(100L), eq(req));
    }

    @Test
    @DisplayName("getFeedbackByRequestId → 200")
    void getFeedbackByRequestId_returns200() {
        when(feedbackService.getFeedbackByRequestId(10L)).thenReturn(sampleFeedback);

        ResponseEntity<FeedbackResponse> resp = controller.getFeedbackByRequestId(10L);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getRequestId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("getFeedbacksByCitizenId → 200 with list")
    void getFeedbacksByCitizenId_returns200() {
        when(feedbackService.getFeedbacksByCitizenId(50L))
                .thenReturn(List.of(sampleFeedback));

        ResponseEntity<List<FeedbackResponse>> resp = controller.getFeedbacksByCitizenId(50L);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("getFeedbacksByCitizenId → 200 with empty list")
    void getFeedbacksByCitizenId_emptyList() {
        when(feedbackService.getFeedbacksByCitizenId(anyLong()))
                .thenReturn(Collections.emptyList());

        ResponseEntity<List<FeedbackResponse>> resp = controller.getFeedbacksByCitizenId(999L);

        assertThat(resp.getBody()).isEmpty();
    }

    @Test
    @DisplayName("getSatisfactionMetricByOfficerId → 200")
    void getSatisfactionMetricByOfficerId_returns200() {
        when(feedbackService.getSatisfactionMetricByOfficerId(200L)).thenReturn(sampleMetric);

        ResponseEntity<SatisfactionMetricResponse> resp =
                controller.getSatisfactionMetricByOfficerId(200L);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getAverageScore()).isEqualTo(4.5);
    }

    @Test
    @DisplayName("getAllSatisfactionMetrics → 200 (leaderboard)")
    void getAllSatisfactionMetrics_returns200() {
        when(feedbackService.getAllSatisfactionMetrics()).thenReturn(List.of(sampleMetric));

        ResponseEntity<List<SatisfactionMetricResponse>> resp =
                controller.getAllSatisfactionMetrics();

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSize(1);
    }
}
