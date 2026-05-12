package com.civicconnect.resolution.controller;

import com.civicconnect.resolution.dto.response.ResolutionValidationResponse;
import com.civicconnect.resolution.entity.Resolution;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Internal validation endpoint — used by compliance-service. Plain Mockito unit tests.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ResolutionValidationController Tests")
class ResolutionValidationControllerTest {

    @Mock private ResolutionRepository resolutionRepository;

    @InjectMocks
    private ResolutionValidationController controller;

    @Test
    @DisplayName("getResolution → 200 with exists=true when found")
    void getResolution_existingId() {
        Resolution r = Resolution.builder()
                .resolutionId(1L)
                .requestId(10L)
                .officerUserId(200L)
                .status(ResolutionStatus.IN_PROGRESS)
                .build();
        when(resolutionRepository.findById(1L)).thenReturn(Optional.of(r));

        ResponseEntity<ResolutionValidationResponse> resp = controller.getResolution(1L);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getResolutionId()).isEqualTo(1L);
        assertThat(resp.getBody().getRequestId()).isEqualTo(10L);
        assertThat(resp.getBody().getOfficerUserId()).isEqualTo(200L);
        assertThat(resp.getBody().getStatus()).isEqualTo("IN_PROGRESS");
        assertThat(resp.getBody().isExists()).isTrue();
    }

    @Test
    @DisplayName("getResolution → 200 with exists=false when not found")
    void getResolution_missingId() {
        when(resolutionRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<ResolutionValidationResponse> resp = controller.getResolution(99L);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getResolutionId()).isEqualTo(99L);
        assertThat(resp.getBody().isExists()).isFalse();
    }
}
