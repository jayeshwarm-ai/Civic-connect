package com.civicconnect.citizen.controller;

import com.civicconnect.citizen.dto.response.CitizenValidationResponse;
import com.civicconnect.citizen.entity.Citizen;
import com.civicconnect.citizen.enums.UserStatus;
import com.civicconnect.citizen.repository.CitizenRepository;
import org.junit.jupiter.api.BeforeEach;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("CitizenValidationController Tests")
class CitizenValidationControllerTest {

    @Mock private CitizenRepository citizenRepository;

    @InjectMocks
    private CitizenValidationController controller;

    private Citizen activeCitizen;

    @BeforeEach
    void setUp() {
        activeCitizen = Citizen.builder()
                .citizenId(1L)
                .userId(100L)
                .name("Jane")
                .email("jane@example.com")
                .accountStatus(UserStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("validateCitizen → 200 with exists=true when found")
    void validateCitizen_existing() {
        when(citizenRepository.findById(1L)).thenReturn(Optional.of(activeCitizen));

        ResponseEntity<CitizenValidationResponse> resp = controller.validateCitizen(1L);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getCitizenId()).isEqualTo(1L);
        assertThat(resp.getBody().getUserId()).isEqualTo(100L);
        assertThat(resp.getBody().getName()).isEqualTo("Jane");
        assertThat(resp.getBody().getAccountStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(resp.getBody().isExists()).isTrue();
    }

    @Test
    @DisplayName("validateCitizen → 200 with exists=false when not found")
    void validateCitizen_missing() {
        when(citizenRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<CitizenValidationResponse> resp = controller.validateCitizen(99L);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().isExists()).isFalse();
        assertThat(resp.getBody().getCitizenId()).isEqualTo(99L);
    }

    @Test
    @DisplayName("existsById → true when present")
    void existsById_true() {
        when(citizenRepository.existsById(1L)).thenReturn(true);

        ResponseEntity<Boolean> resp = controller.existsById(1L);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isTrue();
    }

    @Test
    @DisplayName("existsById → false when missing")
    void existsById_false() {
        when(citizenRepository.existsById(99L)).thenReturn(false);

        ResponseEntity<Boolean> resp = controller.existsById(99L);

        assertThat(resp.getBody()).isFalse();
    }

    @Test
    @DisplayName("getByUserId → 200 with exists=true when found")
    void getByUserId_existing() {
        when(citizenRepository.findByUserId(100L)).thenReturn(Optional.of(activeCitizen));

        ResponseEntity<CitizenValidationResponse> resp = controller.getByUserId(100L);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getUserId()).isEqualTo(100L);
        assertThat(resp.getBody().isExists()).isTrue();
    }

    @Test
    @DisplayName("getByUserId → 200 with exists=false when not found")
    void getByUserId_missing() {
        when(citizenRepository.findByUserId(999L)).thenReturn(Optional.empty());

        ResponseEntity<CitizenValidationResponse> resp = controller.getByUserId(999L);

        assertThat(resp.getBody().isExists()).isFalse();
        assertThat(resp.getBody().getUserId()).isEqualTo(999L);
    }
}
