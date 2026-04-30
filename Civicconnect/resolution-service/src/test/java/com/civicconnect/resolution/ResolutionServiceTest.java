package com.civicconnect.resolution;

import com.civicconnect.resolution.dto.request.CreateResolutionRequest;
import com.civicconnect.resolution.dto.request.CreateWorkflowStepRequest;
import com.civicconnect.resolution.dto.request.UpdateWorkflowStepRequest;
import com.civicconnect.resolution.dto.response.ResolutionResponse;
import com.civicconnect.resolution.dto.response.WorkflowStepResponse;
import com.civicconnect.resolution.entity.Resolution;
import com.civicconnect.resolution.entity.WorkflowStep;
import com.civicconnect.resolution.enums.ResolutionStatus;
import com.civicconnect.resolution.enums.WorkflowStepStatus;
import com.civicconnect.resolution.exception.InvalidOperationException;
import com.civicconnect.resolution.exception.ResourceNotFoundException;
import com.civicconnect.resolution.feign.IdentityFeignClient;
import com.civicconnect.resolution.feign.NotificationFeignClient;
import com.civicconnect.resolution.feign.ServiceRequestFeignClient;
import com.civicconnect.resolution.feign.dto.ServiceRequestValidationResponse;
import com.civicconnect.resolution.feign.dto.UserValidationResponse;
import com.civicconnect.resolution.repository.ResolutionRepository;
import com.civicconnect.resolution.repository.WorkflowStepRepository;
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
@DisplayName("ResolutionService Tests")
class ResolutionServiceTest {

    @Mock private ResolutionRepository      resolutionRepository;
    @Mock private WorkflowStepRepository    workflowStepRepository;
    @Mock private ServiceRequestFeignClient serviceRequestFeignClient;
    @Mock private IdentityFeignClient       identityFeignClient;
    @Mock private NotificationFeignClient   notificationFeignClient;

    @InjectMocks
    private ResolutionService resolutionService;

    private Resolution inProgressResolution;
    private WorkflowStep pendingStep;
    private ServiceRequestValidationResponse assignedRequest;
    private UserValidationResponse serviceOfficer;

    @BeforeEach
    void setUp() {
        inProgressResolution = Resolution.builder()
                .resolutionId(1L)
                .requestId(10L)
                .officerUserId(200L)
                .officerName("Officer Bob")
                .citizenUserId(100L)
                .actions("Fix pothole")
                .status(ResolutionStatus.IN_PROGRESS)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        pendingStep = WorkflowStep.builder()
                .stepId(1L)
                .resolution(inProgressResolution)
                .description("Assess damage")
                .assignedToUserId(200L)
                .assignedToUserName("Officer Bob")
                .status(WorkflowStepStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        assignedRequest = ServiceRequestValidationResponse.builder()
                .requestId(10L)
                .citizenId(50L)
                .citizenUserId(100L)
                .assignedOfficerUserId(200L)
                .status("ASSIGNED")
                .exists(true)
                .build();

        serviceOfficer = UserValidationResponse.builder()
                .userId(200L)
                .name("Officer Bob")
                .role("SERVICE_OFFICER")
                .status("ACTIVE")
                .exists(true)
                .build();
    }

    // ── createResolution ──────────────────────────────────────────────────────

    @Test
    @DisplayName("Should create resolution successfully for ASSIGNED request")
    void shouldCreateResolutionSuccessfully() {
        CreateResolutionRequest req = new CreateResolutionRequest();
        req.setRequestId(10L);
        req.setActions("Fix pothole on Main St");

        when(serviceRequestFeignClient.getRequest(10L)).thenReturn(assignedRequest);
        when(resolutionRepository.existsByRequestId(10L)).thenReturn(false);
        when(identityFeignClient.validateUser(200L)).thenReturn(serviceOfficer);
        when(resolutionRepository.save(any())).thenReturn(inProgressResolution);
        doNothing().when(serviceRequestFeignClient).updateStatus(anyLong(), any());
        doNothing().when(identityFeignClient).writeAuditLog(any());
        doNothing().when(notificationFeignClient).sendNotification(any());

        ResolutionResponse response = resolutionService.createResolution(req, 200L);

        assertThat(response.getResolutionId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(ResolutionStatus.IN_PROGRESS);
        assertThat(response.getOfficerName()).isEqualTo("Officer Bob");
        verify(resolutionRepository).save(any(Resolution.class));
        verify(serviceRequestFeignClient).updateStatus(eq(10L), any());
        verify(notificationFeignClient).sendNotification(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when service request not found")
    void shouldThrowWhenRequestNotFound() {
        CreateResolutionRequest req = new CreateResolutionRequest();
        req.setRequestId(99L);
        req.setActions("Test");

        when(serviceRequestFeignClient.getRequest(99L))
                .thenReturn(ServiceRequestValidationResponse.builder()
                        .requestId(99L).exists(false).build());

        assertThatThrownBy(() -> resolutionService.createResolution(req, 200L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ServiceRequest not found");
    }

    @Test
    @DisplayName("Should throw InvalidOperationException when request is not ASSIGNED")
    void shouldThrowWhenRequestNotAssigned() {
        CreateResolutionRequest req = new CreateResolutionRequest();
        req.setRequestId(10L);
        req.setActions("Test");

        ServiceRequestValidationResponse inProgressReq = ServiceRequestValidationResponse.builder()
                .requestId(10L).citizenUserId(100L)
                .assignedOfficerUserId(200L).status("IN_PROGRESS").exists(true).build();

        when(serviceRequestFeignClient.getRequest(10L)).thenReturn(inProgressReq);

        assertThatThrownBy(() -> resolutionService.createResolution(req, 200L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("ASSIGNED requests");
    }

    @Test
    @DisplayName("Should throw InvalidOperationException when resolution already exists")
    void shouldThrowWhenResolutionAlreadyExists() {
        CreateResolutionRequest req = new CreateResolutionRequest();
        req.setRequestId(10L);
        req.setActions("Test");

        when(serviceRequestFeignClient.getRequest(10L)).thenReturn(assignedRequest);
        when(resolutionRepository.existsByRequestId(10L)).thenReturn(true);

        assertThatThrownBy(() -> resolutionService.createResolution(req, 200L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("resolution already exists");
    }

    @Test
    @DisplayName("Should throw InvalidOperationException when not the assigned officer")
    void shouldThrowWhenNotAssignedOfficer() {
        CreateResolutionRequest req = new CreateResolutionRequest();
        req.setRequestId(10L);
        req.setActions("Test");

        when(serviceRequestFeignClient.getRequest(10L)).thenReturn(assignedRequest);

        // Officer 999 is not the assigned officer (200 is)
        assertThatThrownBy(() -> resolutionService.createResolution(req, 999L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("assigned officer");
    }

    // ── addWorkflowStep ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Should add workflow step successfully")
    void shouldAddWorkflowStepSuccessfully() {
        CreateWorkflowStepRequest req = new CreateWorkflowStepRequest();
        req.setDescription("Assess road damage");
        req.setAssignedToUserId(200L);

        when(resolutionRepository.findById(1L)).thenReturn(Optional.of(inProgressResolution));
        when(identityFeignClient.validateUser(200L)).thenReturn(serviceOfficer);
        when(workflowStepRepository.save(any())).thenReturn(pendingStep);
        doNothing().when(identityFeignClient).writeAuditLog(any());

        WorkflowStepResponse response = resolutionService.addWorkflowStep(1L, req, 200L);

        assertThat(response.getStepId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(WorkflowStepStatus.PENDING);
        verify(workflowStepRepository).save(any(WorkflowStep.class));
    }

    @Test
    @DisplayName("Should throw InvalidOperationException when assigning step to non-SERVICE_OFFICER")
    void shouldThrowWhenStepAssigneeNotOfficer() {
        CreateWorkflowStepRequest req = new CreateWorkflowStepRequest();
        req.setDescription("Test step");
        req.setAssignedToUserId(300L);

        UserValidationResponse departmentHead = UserValidationResponse.builder()
                .userId(300L).name("Head").role("DEPARTMENT_HEAD").exists(true).build();

        when(resolutionRepository.findById(1L)).thenReturn(Optional.of(inProgressResolution));
        when(identityFeignClient.validateUser(300L)).thenReturn(departmentHead);

        assertThatThrownBy(() -> resolutionService.addWorkflowStep(1L, req, 200L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("SERVICE_OFFICERs");
    }

    @Test
    @DisplayName("Should throw InvalidOperationException when not resolution officer adds step")
    void shouldThrowWhenNotResolutionOfficerAddsStep() {
        CreateWorkflowStepRequest req = new CreateWorkflowStepRequest();
        req.setDescription("Test step");
        req.setAssignedToUserId(200L);

        when(resolutionRepository.findById(1L)).thenReturn(Optional.of(inProgressResolution));

        // Officer 999 is not the resolution owner (200 is)
        assertThatThrownBy(() -> resolutionService.addWorkflowStep(1L, req, 999L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Only the resolution officer");
    }

    // ── updateWorkflowStepStatus ──────────────────────────────────────────────

    @Test
    @DisplayName("Should update step PENDING → IN_PROGRESS successfully")
    void shouldUpdateStepToInProgress() {
        UpdateWorkflowStepRequest req = new UpdateWorkflowStepRequest();
        req.setStatus(WorkflowStepStatus.IN_PROGRESS);

        when(workflowStepRepository.findById(1L)).thenReturn(Optional.of(pendingStep));
        when(workflowStepRepository.save(any())).thenReturn(pendingStep);
        doNothing().when(identityFeignClient).writeAuditLog(any());

        WorkflowStepResponse response = resolutionService.updateWorkflowStepStatus(1L, req, 200L);

        assertThat(response).isNotNull();
        verify(workflowStepRepository).save(any(WorkflowStep.class));
    }

    @Test
    @DisplayName("Should complete resolution and push RESOLVED when all steps done")
    void shouldCompleteResolutionWhenAllStepsDone() {
        UpdateWorkflowStepRequest req = new UpdateWorkflowStepRequest();
        req.setStatus(WorkflowStepStatus.COMPLETED);

        // Set step to IN_PROGRESS so transition to COMPLETED is valid
        pendingStep.setStatus(WorkflowStepStatus.IN_PROGRESS);

        when(workflowStepRepository.findById(1L)).thenReturn(Optional.of(pendingStep));
        when(workflowStepRepository.save(any())).thenReturn(pendingStep);
        // No remaining incomplete steps
        when(workflowStepRepository.existsByResolution_ResolutionIdAndStatusNot(
                1L, WorkflowStepStatus.COMPLETED)).thenReturn(false);
        when(resolutionRepository.save(any())).thenReturn(inProgressResolution);
        doNothing().when(serviceRequestFeignClient).updateStatus(anyLong(), any());
        doNothing().when(notificationFeignClient).sendNotification(any());
        doNothing().when(identityFeignClient).writeAuditLog(any());

        resolutionService.updateWorkflowStepStatus(1L, req, 200L);

        verify(resolutionRepository).save(any(Resolution.class));
        verify(serviceRequestFeignClient).updateStatus(eq(10L), argThat(
                s -> "RESOLVED".equals(s.getStatus())));
        verify(notificationFeignClient).sendNotification(any());
    }

    @Test
    @DisplayName("Should NOT complete resolution when steps still pending")
    void shouldNotCompleteResolutionWhenStepsPending() {
        UpdateWorkflowStepRequest req = new UpdateWorkflowStepRequest();
        req.setStatus(WorkflowStepStatus.COMPLETED);

        pendingStep.setStatus(WorkflowStepStatus.IN_PROGRESS);

        when(workflowStepRepository.findById(1L)).thenReturn(Optional.of(pendingStep));
        when(workflowStepRepository.save(any())).thenReturn(pendingStep);
        // Still has incomplete steps
        when(workflowStepRepository.existsByResolution_ResolutionIdAndStatusNot(
                1L, WorkflowStepStatus.COMPLETED)).thenReturn(true);
        doNothing().when(identityFeignClient).writeAuditLog(any());

        resolutionService.updateWorkflowStepStatus(1L, req, 200L);

        verify(resolutionRepository, never()).save(any());
        verify(serviceRequestFeignClient, never()).updateStatus(anyLong(), any());
    }

    @Test
    @DisplayName("Should throw InvalidOperationException on invalid step transition PENDING → COMPLETED")
    void shouldThrowOnInvalidStepTransition() {
        UpdateWorkflowStepRequest req = new UpdateWorkflowStepRequest();
        req.setStatus(WorkflowStepStatus.COMPLETED); // PENDING → COMPLETED is invalid

        when(workflowStepRepository.findById(1L)).thenReturn(Optional.of(pendingStep));

        assertThatThrownBy(() -> resolutionService.updateWorkflowStepStatus(1L, req, 200L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Invalid step status transition");
    }

    @Test
    @DisplayName("Should throw InvalidOperationException when wrong user updates step")
    void shouldThrowWhenWrongUserUpdatesStep() {
        UpdateWorkflowStepRequest req = new UpdateWorkflowStepRequest();
        req.setStatus(WorkflowStepStatus.IN_PROGRESS);

        when(workflowStepRepository.findById(1L)).thenReturn(Optional.of(pendingStep));

        // User 999 is not the assigned user (200 is)
        assertThatThrownBy(() -> resolutionService.updateWorkflowStepStatus(1L, req, 999L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Only the assigned user");
    }

    @Test
    @DisplayName("Should throw InvalidOperationException when trying to set PENDING status")
    void shouldThrowWhenSettingPendingStatus() {
        UpdateWorkflowStepRequest req = new UpdateWorkflowStepRequest();
        req.setStatus(WorkflowStepStatus.PENDING);

        when(workflowStepRepository.findById(1L)).thenReturn(Optional.of(pendingStep));

        assertThatThrownBy(() -> resolutionService.updateWorkflowStepStatus(1L, req, 200L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("back to PENDING");
    }

    // ── getResolutionByRequestId ──────────────────────────────────────────────

    @Test
    @DisplayName("Should return resolution by request ID")
    void shouldReturnResolutionByRequestId() {
        when(resolutionRepository.findByRequestId(10L))
                .thenReturn(Optional.of(inProgressResolution));

        ResolutionResponse response = resolutionService.getResolutionByRequestId(10L);

        assertThat(response.getRequestId()).isEqualTo(10L);
        assertThat(response.getOfficerUserId()).isEqualTo(200L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when no resolution for request")
    void shouldThrowWhenNoResolutionForRequest() {
        when(resolutionRepository.findByRequestId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resolutionService.getResolutionByRequestId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Resolution not found for requestId");
    }
}
