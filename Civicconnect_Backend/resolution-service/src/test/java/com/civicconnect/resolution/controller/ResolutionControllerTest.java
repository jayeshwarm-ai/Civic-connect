package com.civicconnect.resolution.controller;

import com.civicconnect.resolution.dto.request.CreateResolutionRequest;
import com.civicconnect.resolution.dto.request.CreateWorkflowStepRequest;
import com.civicconnect.resolution.dto.request.UpdateWorkflowStepRequest;
import com.civicconnect.resolution.dto.response.ResolutionResponse;
import com.civicconnect.resolution.dto.response.WorkflowStepResponse;
import com.civicconnect.resolution.enums.ResolutionStatus;
import com.civicconnect.resolution.enums.WorkflowStepStatus;
import com.civicconnect.resolution.service.ResolutionService;
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

/**
 * Controller-layer unit tests — plain JUnit 5 + Mockito (no Spring context).
 * Verifies that the controller:
 *   - delegates correctly to the service
 *   - returns the right HTTP status
 *   - extracts userId from Authentication for endpoints that need it
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ResolutionController Tests")
class ResolutionControllerTest {

    @Mock private ResolutionService resolutionService;

    @InjectMocks
    private ResolutionController resolutionController;

    private UsernamePasswordAuthenticationToken authWithUserId(Long userId) {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("user", null, Collections.emptyList());
        auth.setDetails(userId);
        return auth;
    }

    private ResolutionResponse sampleResolution;
    private WorkflowStepResponse sampleStep;

    @BeforeEach
    void setUp() {
        sampleResolution = ResolutionResponse.builder()
                .resolutionId(1L)
                .requestId(10L)
                .officerUserId(200L)
                .officerName("Officer Bob")
                .status(ResolutionStatus.IN_PROGRESS)
                .build();

        sampleStep = WorkflowStepResponse.builder()
                .stepId(1L)
                .description("Inspect site")
                .assignedToUserId(200L)
                .assignedToUserName("Officer Bob")
                .status(WorkflowStepStatus.PENDING)
                .build();
    }

    // ── POST /api/v1/resolutions ──────────────────────────────────────────────

    @Test
    @DisplayName("createResolution → 201 with body, delegates with extracted userId")
    void createResolution_returns201() {
        CreateResolutionRequest req = new CreateResolutionRequest();
        req.setRequestId(10L);
        req.setActions("Fix the issue");
        when(resolutionService.createResolution(eq(req), eq(200L))).thenReturn(sampleResolution);

        ResponseEntity<ResolutionResponse> resp =
                resolutionController.createResolution(req, authWithUserId(200L));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody()).isEqualTo(sampleResolution);
        verify(resolutionService).createResolution(eq(req), eq(200L));
    }

    // ── GET /api/v1/resolutions/{resolutionId} ────────────────────────────────

    @Test
    @DisplayName("getResolutionById → 200 with the resolution")
    void getResolutionById_returns200() {
        when(resolutionService.getResolutionById(1L)).thenReturn(sampleResolution);

        ResponseEntity<ResolutionResponse> resp = resolutionController.getResolutionById(1L);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getResolutionId()).isEqualTo(1L);
    }

    // ── GET /api/v1/resolutions/by-request/{requestId} ────────────────────────

    @Test
    @DisplayName("getResolutionByRequestId → 200 with the resolution")
    void getResolutionByRequestId_returns200() {
        when(resolutionService.getResolutionByRequestId(10L)).thenReturn(sampleResolution);

        ResponseEntity<ResolutionResponse> resp = resolutionController.getResolutionByRequestId(10L);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getRequestId()).isEqualTo(10L);
    }

    // ── GET /api/v1/resolutions/officer/{officerId} ───────────────────────────

    @Test
    @DisplayName("getResolutionsByOfficerId → 200 with list")
    void getResolutionsByOfficerId_returns200() {
        when(resolutionService.getResolutionsByOfficerId(200L))
                .thenReturn(List.of(sampleResolution));

        ResponseEntity<List<ResolutionResponse>> resp =
                resolutionController.getResolutionsByOfficerId(200L);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("getResolutionsByOfficerId → 200 with empty list when officer has none")
    void getResolutionsByOfficerId_emptyList() {
        when(resolutionService.getResolutionsByOfficerId(anyLong()))
                .thenReturn(Collections.emptyList());

        ResponseEntity<List<ResolutionResponse>> resp =
                resolutionController.getResolutionsByOfficerId(999L);

        assertThat(resp.getBody()).isEmpty();
    }

    // ── POST /api/v1/resolutions/{resolutionId}/steps ─────────────────────────

    @Test
    @DisplayName("addWorkflowStep → 201, delegates with extracted userId")
    void addWorkflowStep_returns201() {
        CreateWorkflowStepRequest req = new CreateWorkflowStepRequest();
        req.setDescription("Inspect site");
        req.setAssignedToUserId(200L);
        when(resolutionService.addWorkflowStep(eq(1L), eq(req), eq(200L))).thenReturn(sampleStep);

        ResponseEntity<WorkflowStepResponse> resp =
                resolutionController.addWorkflowStep(1L, req, authWithUserId(200L));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody().getStepId()).isEqualTo(1L);
        verify(resolutionService).addWorkflowStep(eq(1L), eq(req), eq(200L));
    }

    // ── GET /api/v1/resolutions/{resolutionId}/steps ──────────────────────────

    @Test
    @DisplayName("getWorkflowSteps → 200 with list")
    void getWorkflowSteps_returns200() {
        when(resolutionService.getWorkflowStepsByResolutionId(1L)).thenReturn(List.of(sampleStep));

        ResponseEntity<List<WorkflowStepResponse>> resp =
                resolutionController.getWorkflowSteps(1L);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSize(1);
    }

    // ── PATCH /api/v1/resolutions/steps/{stepId}/status ───────────────────────

    @Test
    @DisplayName("updateWorkflowStepStatus → 200, delegates with extracted userId")
    void updateWorkflowStepStatus_returns200() {
        UpdateWorkflowStepRequest req = new UpdateWorkflowStepRequest();
        req.setStatus(WorkflowStepStatus.IN_PROGRESS);
        when(resolutionService.updateWorkflowStepStatus(eq(1L), eq(req), eq(200L)))
                .thenReturn(sampleStep);

        ResponseEntity<WorkflowStepResponse> resp =
                resolutionController.updateWorkflowStepStatus(1L, req, authWithUserId(200L));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isEqualTo(sampleStep);
        verify(resolutionService).updateWorkflowStepStatus(eq(1L), eq(req), eq(200L));
    }

    @Test
    @DisplayName("extractUserId works for any numeric userId in auth details")
    void extractUserId_handlesIntegerAndLong() {
        // Auth details often arrive as Integer when set by JwtAuthFilter;
        // the controller's extractUserId casts via Number.longValue() so both work.
        UsernamePasswordAuthenticationToken intAuth =
                new UsernamePasswordAuthenticationToken("u", null, Collections.emptyList());
        intAuth.setDetails(Integer.valueOf(42));

        CreateResolutionRequest req = new CreateResolutionRequest();
        req.setRequestId(10L);
        req.setActions("x");
        when(resolutionService.createResolution(eq(req), eq(42L))).thenReturn(sampleResolution);

        resolutionController.createResolution(req, intAuth);

        verify(resolutionService).createResolution(eq(req), eq(42L));
    }
}
