package com.civicconnect.citizen.controller;

import com.civicconnect.citizen.dto.request.CitizenRegistrationRequest;
import com.civicconnect.citizen.dto.request.DocumentVerificationRequest;
import com.civicconnect.citizen.dto.request.UpdateCitizenProfileRequest;
import com.civicconnect.citizen.dto.response.CitizenDocumentResponse;
import com.civicconnect.citizen.dto.response.CitizenResponse;
import com.civicconnect.citizen.enums.DocType;
import com.civicconnect.citizen.service.CitizenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CitizenController Tests")
class CitizenControllerTest {

    @Mock private CitizenService citizenService;

    @InjectMocks
    private CitizenController controller;

    private UsernamePasswordAuthenticationToken authWithUserId(Long userId) {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("user", null, Collections.emptyList());
        auth.setDetails(userId);
        return auth;
    }

    private CitizenResponse sampleCitizen;
    private CitizenDocumentResponse sampleDoc;

    @BeforeEach
    void setUp() {
        sampleCitizen = CitizenResponse.builder()
                .citizenId(1L)
                .userId(100L)
                .name("Jane")
                .build();
        sampleDoc = CitizenDocumentResponse.builder()
                .documentId(1L)
                .citizenId(1L)
                .docType(DocType.ID_PROOF)
                .build();
    }

    @Test
    @DisplayName("registerCitizen → 201")
    void registerCitizen_returns201() {
        CitizenRegistrationRequest req = new CitizenRegistrationRequest();
        when(citizenService.registerCitizen(eq(req))).thenReturn(sampleCitizen);

        ResponseEntity<CitizenResponse> resp = controller.registerCitizen(req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(citizenService).registerCitizen(eq(req));
    }

    @Test
    @DisplayName("getAllCitizens → 200 with list")
    void getAllCitizens_returns200() {
        when(citizenService.getAllCitizens()).thenReturn(List.of(sampleCitizen));

        ResponseEntity<List<CitizenResponse>> resp = controller.getAllCitizens();

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("getCitizenById → 200")
    void getCitizenById_returns200() {
        when(citizenService.getCitizenById(1L)).thenReturn(sampleCitizen);

        ResponseEntity<CitizenResponse> resp = controller.getCitizenById(1L);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getCitizenId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getCitizenByUserId → 200")
    void getCitizenByUserId_returns200() {
        when(citizenService.getCitizenByUserId(100L)).thenReturn(sampleCitizen);

        ResponseEntity<CitizenResponse> resp = controller.getCitizenByUserId(100L);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getUserId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("getMyProfile → 200, delegates with extracted userId")
    void getMyProfile_returns200() {
        when(citizenService.getCitizenByUserId(100L)).thenReturn(sampleCitizen);

        ResponseEntity<CitizenResponse> resp = controller.getMyProfile(authWithUserId(100L));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(citizenService).getCitizenByUserId(100L);
    }

    @Test
    @DisplayName("updateMyProfile → 200")
    void updateMyProfile_returns200() {
        UpdateCitizenProfileRequest req = new UpdateCitizenProfileRequest();
        when(citizenService.updateMyProfile(eq(req), eq(100L))).thenReturn(sampleCitizen);

        ResponseEntity<CitizenResponse> resp =
                controller.updateMyProfile(req, authWithUserId(100L));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(citizenService).updateMyProfile(eq(req), eq(100L));
    }

    @Test
    @DisplayName("uploadMyDocument → 201")
    void uploadMyDocument_returns201() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "id.pdf", "application/pdf", "fake".getBytes());
        when(citizenService.uploadMyDocument(eq(DocType.ID_PROOF), eq(file), eq(100L)))
                .thenReturn(sampleDoc);

        ResponseEntity<CitizenDocumentResponse> resp =
                controller.uploadMyDocument(DocType.ID_PROOF, file, authWithUserId(100L));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(citizenService).uploadMyDocument(eq(DocType.ID_PROOF), eq(file), eq(100L));
    }

    @Test
    @DisplayName("getMyDocuments → 200")
    void getMyDocuments_returns200() {
        when(citizenService.getMyDocuments(100L)).thenReturn(List.of(sampleDoc));

        ResponseEntity<List<CitizenDocumentResponse>> resp =
                controller.getMyDocuments(authWithUserId(100L));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("getDocuments → 200")
    void getDocuments_returns200() {
        when(citizenService.getDocumentsByCitizenId(1L)).thenReturn(List.of(sampleDoc));

        ResponseEntity<List<CitizenDocumentResponse>> resp = controller.getDocuments(1L);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("getPendingDocuments → 200")
    void getPendingDocuments_returns200() {
        when(citizenService.getPendingDocuments()).thenReturn(List.of(sampleDoc));

        ResponseEntity<List<CitizenDocumentResponse>> resp = controller.getPendingDocuments();

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("verifyDocument → 200")
    void verifyDocument_returns200() {
        DocumentVerificationRequest req = new DocumentVerificationRequest();
        when(citizenService.verifyDocument(eq(1L), eq(req), eq(200L))).thenReturn(sampleDoc);

        ResponseEntity<CitizenDocumentResponse> resp =
                controller.verifyDocument(1L, req, authWithUserId(200L));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(citizenService).verifyDocument(eq(1L), eq(req), eq(200L));
    }

    @Test
    @DisplayName("downloadDocument → 404 when file URI is not a real file")
    void downloadDocument_missingFile() {
        // We give the service a fake URI; the controller's UrlResource.exists() will be false.
        CitizenDocumentResponse missingDoc = CitizenDocumentResponse.builder()
                .documentId(99L)
                .fileUri("/tmp/does-not-exist-xyz-" + System.nanoTime() + ".pdf")
                .build();
        when(citizenService.getDocumentById(99L)).thenReturn(missingDoc);

        ResponseEntity<Resource> resp = controller.downloadDocument(99L);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("deactivateCitizen → 204")
    void deactivateCitizen_returns204() {
        doNothing().when(citizenService).deactivateCitizen(eq(1L), eq(200L));

        ResponseEntity<Void> resp = controller.deactivateCitizen(1L, authWithUserId(200L));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(citizenService).deactivateCitizen(eq(1L), eq(200L));
    }
}
