package com.civicconnect.feedback.controller;

import com.civicconnect.feedback.entity.Feedback;
import com.civicconnect.feedback.entity.SatisfactionMetric;
import com.civicconnect.feedback.repository.FeedbackRepository;
import com.civicconnect.feedback.repository.SatisfactionMetricRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedbackStatsController Tests")
class FeedbackStatsControllerTest {

    @Mock private FeedbackRepository          feedbackRepository;
    @Mock private SatisfactionMetricRepository satisfactionMetricRepository;

    @InjectMocks
    private FeedbackStatsController controller;

    @Test
    @DisplayName("getStats → 200 with averages and top officer")
    void getStats_returnsAggregated() {
        Feedback f1 = Feedback.builder().feedbackId(1L).rating(5).build();
        Feedback f2 = Feedback.builder().feedbackId(2L).rating(3).build();

        SatisfactionMetric top = new SatisfactionMetric();
        top.setOfficerUserId(200L);
        top.setOfficerName("Officer Bob");
        top.setTotalRatingSum(20L);
        top.setTotalFeedbackCount(5L);
        top.setAverageScore(4.0);
        // averageScore computed via getter inside entity

        when(feedbackRepository.count()).thenReturn(2L);
        when(feedbackRepository.findAll()).thenReturn(List.of(f1, f2));
        when(satisfactionMetricRepository.findAllByOrderByAverageScoreDesc())
                .thenReturn(List.of(top));

        ResponseEntity<FeedbackStatsController.FeedbackStatsResponse> resp = controller.getStats();

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        FeedbackStatsController.FeedbackStatsResponse body = resp.getBody();
        assertThat(body.getTotalFeedbacks()).isEqualTo(2L);
        assertThat(body.getAverageRating()).isEqualTo(4.0, within(0.001));
        assertThat(body.getOfficersRated()).isEqualTo(1L);
        assertThat(body.getTopOfficerName()).isEqualTo("Officer Bob");
    }

    @Test
    @DisplayName("getStats → returns sensible defaults when empty")
    void getStats_emptyState() {
        when(feedbackRepository.count()).thenReturn(0L);
        when(feedbackRepository.findAll()).thenReturn(Collections.emptyList());
        when(satisfactionMetricRepository.findAllByOrderByAverageScoreDesc())
                .thenReturn(Collections.emptyList());

        ResponseEntity<FeedbackStatsController.FeedbackStatsResponse> resp = controller.getStats();

        FeedbackStatsController.FeedbackStatsResponse body = resp.getBody();
        assertThat(body.getTotalFeedbacks()).isZero();
        assertThat(body.getAverageRating()).isZero();
        assertThat(body.getOfficersRated()).isZero();
        assertThat(body.getTopOfficerName()).isEqualTo("N/A");
        assertThat(body.getTopOfficerScore()).isZero();
    }
}
