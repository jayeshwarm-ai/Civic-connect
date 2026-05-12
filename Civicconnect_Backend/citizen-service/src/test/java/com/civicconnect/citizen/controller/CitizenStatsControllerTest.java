package com.civicconnect.citizen.controller;

import com.civicconnect.citizen.repository.CitizenRepository;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("CitizenStatsController Tests")
class CitizenStatsControllerTest {

    @Mock private CitizenRepository citizenRepository;

    @InjectMocks
    private CitizenStatsController controller;

    @Test
    @DisplayName("getStats → 200 with total citizens")
    void getStats_returnsTotal() {
        when(citizenRepository.count()).thenReturn(42L);

        ResponseEntity<CitizenStatsController.CitizenStatsResponse> resp = controller.getStats();

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getTotalCitizens()).isEqualTo(42L);
    }

    @Test
    @DisplayName("getStats → returns zero when no citizens")
    void getStats_handlesEmpty() {
        when(citizenRepository.count()).thenReturn(0L);

        ResponseEntity<CitizenStatsController.CitizenStatsResponse> resp = controller.getStats();

        assertThat(resp.getBody().getTotalCitizens()).isZero();
    }
}
