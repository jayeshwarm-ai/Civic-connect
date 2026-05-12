package com.civicconnect.resolution.controller;

import com.civicconnect.resolution.enums.ResolutionStatus;
import com.civicconnect.resolution.repository.ResolutionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Internal stats endpoint — used by reporting-service. Plain Mockito unit tests.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ResolutionStatsController Tests")
class ResolutionStatsControllerTest {

    @Mock private ResolutionRepository resolutionRepository;

    @InjectMocks
    private ResolutionStatsController controller;

    @Test
    @DisplayName("getStats → 200 with total, in-progress, completed counts")
    void getStats_returnsAggregatedCounts() {
        when(resolutionRepository.count()).thenReturn(10L);
        when(resolutionRepository.countByStatus(ResolutionStatus.IN_PROGRESS)).thenReturn(4L);
        when(resolutionRepository.countByStatus(ResolutionStatus.COMPLETED)).thenReturn(6L);

        ResponseEntity<ResolutionStatsController.ResolutionStatsResponse> resp = controller.getStats();

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getTotal()).isEqualTo(10L);
        assertThat(resp.getBody().getInProgress()).isEqualTo(4L);
        assertThat(resp.getBody().getCompleted()).isEqualTo(6L);
    }

    @Test
    @DisplayName("getStats → returns zeros when repository is empty")
    void getStats_handlesEmpty() {
        when(resolutionRepository.count()).thenReturn(0L);
        when(resolutionRepository.countByStatus(ResolutionStatus.IN_PROGRESS)).thenReturn(0L);
        when(resolutionRepository.countByStatus(ResolutionStatus.COMPLETED)).thenReturn(0L);

        ResponseEntity<ResolutionStatsController.ResolutionStatsResponse> resp = controller.getStats();

        assertThat(resp.getBody().getTotal()).isZero();
        assertThat(resp.getBody().getInProgress()).isZero();
        assertThat(resp.getBody().getCompleted()).isZero();
    }
}
