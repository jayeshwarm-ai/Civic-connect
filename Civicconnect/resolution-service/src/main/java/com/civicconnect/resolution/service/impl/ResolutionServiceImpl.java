package com.civicconnect.resolution.service.impl;

import com.civicconnect.resolution.dto.request.CreateResolutionRequest;
import com.civicconnect.resolution.dto.request.CreateWorkflowStepRequest;
import com.civicconnect.resolution.dto.request.UpdateWorkflowStepRequest;
import com.civicconnect.resolution.dto.response.ResolutionResponse;
import com.civicconnect.resolution.dto.response.WorkflowStepResponse;
import com.civicconnect.resolution.entity.Resolution;
import com.civicconnect.resolution.entity.WorkflowStep;
import com.civicconnect.resolution.enums.NotificationCategory;
import com.civicconnect.resolution.enums.ResolutionStatus;
import com.civicconnect.resolution.enums.WorkflowStepStatus;
import com.civicconnect.resolution.exception.InvalidOperationException;
import com.civicconnect.resolution.exception.ResourceNotFoundException;
import com.civicconnect.resolution.feign.IdentityFeignClient;
import com.civicconnect.resolution.feign.NotificationFeignClient;
import com.civicconnect.resolution.feign.ServiceRequestFeignClient;
import com.civicconnect.resolution.feign.dto.*;
import com.civicconnect.resolution.repository.ResolutionRepository;
import com.civicconnect.resolution.repository.WorkflowStepRepository;
import com.civicconnect.resolution.service.ResolutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResolutionServiceImpl implements ResolutionService {

    private final ResolutionRepository     resolutionRepository;
    private final WorkflowStepRepository   workflowStepRepository;
    private final ServiceRequestFeignClient serviceRequestFeignClient;
    private final IdentityFeignClient      identityFeignClient;
    private final NotificationFeignClient  notificationFeignClient;

    @Override
    @Transactional
    public ResolutionResponse createResolution(CreateResolutionRequest request, Long officerUserId) {
        ServiceRequestValidationResponse srData = serviceRequestFeignClient.getRequest(request.getRequestId());
        if (!srData.isExists()) throw new ResourceNotFoundException("ServiceRequest not found with id: " + request.getRequestId());
        if (!"ASSIGNED".equals(srData.getStatus()))
            throw new InvalidOperationException("Resolution can only be created for ASSIGNED requests. Current status: " + srData.getStatus());
        if (!officerUserId.equals(srData.getAssignedOfficerUserId()))
            throw new InvalidOperationException("Only the assigned officer can create a resolution for this request.");
        if (resolutionRepository.existsByRequestId(request.getRequestId()))
            throw new InvalidOperationException("A resolution already exists for requestId: " + request.getRequestId());

        UserValidationResponse officer = identityFeignClient.validateUser(officerUserId);
        String officerName = officer.isExists() ? officer.getName() : "Officer#" + officerUserId;

        Resolution resolution = Resolution.builder()
                .requestId(request.getRequestId()).officerUserId(officerUserId).officerName(officerName)
                .citizenUserId(srData.getCitizenUserId()).actions(request.getActions()).build();
        resolution = resolutionRepository.save(resolution);

        serviceRequestFeignClient.updateStatus(request.getRequestId(), StatusUpdateRequest.builder().status("IN_PROGRESS").build());

        writeAuditLog(officerUserId, "RESOLUTION_CREATED", "RESOLUTION",
                String.valueOf(resolution.getResolutionId()), "Resolution created for requestId: " + request.getRequestId());

        sendNotification(srData.getCitizenUserId(), request.getRequestId(),
                "A resolution has been created for your service request #" + request.getRequestId() + ". Work is now in progress.",
                NotificationCategory.RESOLUTION);
        return mapToResolutionResponse(resolution);
    }

    @Override
    @Transactional
    public WorkflowStepResponse addWorkflowStep(Long resolutionId, CreateWorkflowStepRequest request, Long officerUserId) {
        Resolution resolution = findResolutionById(resolutionId);
        if (resolution.getStatus() != ResolutionStatus.IN_PROGRESS)
            throw new InvalidOperationException("Steps can only be added to IN_PROGRESS resolutions.");
        if (!resolution.getOfficerUserId().equals(officerUserId))
            throw new InvalidOperationException("Only the resolution officer can add workflow steps.");

        UserValidationResponse assignee = identityFeignClient.validateUser(request.getAssignedToUserId());
        if (!assignee.isExists()) throw new ResourceNotFoundException("User", request.getAssignedToUserId());
        if (!"SERVICE_OFFICER".equals(assignee.getRole()))
            throw new InvalidOperationException("Workflow steps can only be assigned to SERVICE_OFFICERs.");

        WorkflowStep step = WorkflowStep.builder()
                .resolution(resolution).description(request.getDescription())
                .assignedToUserId(assignee.getUserId()).assignedToUserName(assignee.getName()).build();
        step = workflowStepRepository.save(step);

        writeAuditLog(officerUserId, "WORKFLOW_STEP_CREATED", "WORKFLOW_STEP",
                String.valueOf(step.getStepId()), "Step added to resolutionId: " + resolutionId);
        return mapToStepResponse(step);
    }

    @Override
    @Transactional
    public WorkflowStepResponse updateWorkflowStepStatus(Long stepId, UpdateWorkflowStepRequest request, Long userId) {
        WorkflowStep step = workflowStepRepository.findById(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkflowStep", stepId));
        if (!step.getAssignedToUserId().equals(userId))
            throw new InvalidOperationException("Only the assigned user can update this workflow step.");
        if (request.getStatus() == WorkflowStepStatus.PENDING)
            throw new InvalidOperationException("Cannot set workflow step status back to PENDING.");

        validateStepTransition(step.getStatus(), request.getStatus());
        step.setStatus(request.getStatus());
        workflowStepRepository.save(step);

        writeAuditLog(userId, "WORKFLOW_STEP_STATUS_UPDATED", "WORKFLOW_STEP",
                String.valueOf(stepId), "Step status updated to: " + request.getStatus());

        if (request.getStatus() == WorkflowStepStatus.COMPLETED) {
            checkAndCompleteResolution(step.getResolution(), userId);
        }
        return mapToStepResponse(step);
    }

    @Override
    @Transactional(readOnly = true)
    public ResolutionResponse getResolutionById(Long resolutionId) {
        return mapToResolutionResponse(findResolutionById(resolutionId));
    }

    @Override
    @Transactional(readOnly = true)
    public ResolutionResponse getResolutionByRequestId(Long requestId) {
        return mapToResolutionResponse(resolutionRepository.findByRequestId(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Resolution not found for requestId: " + requestId)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowStepResponse> getWorkflowStepsByResolutionId(Long resolutionId) {
        findResolutionById(resolutionId);
        return workflowStepRepository.findByResolution_ResolutionIdOrderByCreatedAtAsc(resolutionId)
                .stream().map(this::mapToStepResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResolutionResponse> getResolutionsByOfficerId(Long officerUserId) {
        UserValidationResponse officer = identityFeignClient.validateUser(officerUserId);
        if (!officer.isExists()) throw new ResourceNotFoundException("Officer", officerUserId);
        return resolutionRepository.findByOfficerUserIdOrderByCreatedAtDesc(officerUserId)
                .stream().map(this::mapToResolutionResponse).collect(Collectors.toList());
    }

    private void checkAndCompleteResolution(Resolution resolution, Long userId) {
        boolean anyNotCompleted = workflowStepRepository.existsByResolution_ResolutionIdAndStatusNot(
                resolution.getResolutionId(), WorkflowStepStatus.COMPLETED);
        if (!anyNotCompleted) {
            resolution.setStatus(ResolutionStatus.COMPLETED);
            resolutionRepository.save(resolution);
            serviceRequestFeignClient.updateStatus(resolution.getRequestId(), StatusUpdateRequest.builder().status("RESOLVED").build());
            sendNotification(resolution.getCitizenUserId(), resolution.getRequestId(),
                    "Your service request #" + resolution.getRequestId() + " has been RESOLVED. Please confirm and close it.",
                    NotificationCategory.RESOLUTION);
            writeAuditLog(userId, "RESOLUTION_COMPLETED", "RESOLUTION",
                    String.valueOf(resolution.getResolutionId()), "All steps completed. RequestId: " + resolution.getRequestId());
        }
    }

    private void validateStepTransition(WorkflowStepStatus current, WorkflowStepStatus next) {
        boolean valid = switch (current) {
            case PENDING -> next == WorkflowStepStatus.IN_PROGRESS;
            case IN_PROGRESS -> next == WorkflowStepStatus.COMPLETED;
            case COMPLETED -> false;
        };
        if (!valid) throw new InvalidOperationException("Invalid step status transition: " + current + " → " + next);
    }

    private Resolution findResolutionById(Long resolutionId) {
        return resolutionRepository.findById(resolutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Resolution", resolutionId));
    }

    private void writeAuditLog(Long performedBy, String action, String resource, String resourceId, String detail) {
        try {
            identityFeignClient.writeAuditLog(AuditLogRequest.builder()
                    .performedBy(performedBy).action(action).resource(resource).resourceId(resourceId).detail(detail).build());
        } catch (Exception e) { log.warn("Failed to write audit log: action={}: {}", action, e.getMessage()); }
    }

    private void sendNotification(Long userId, Long requestId, String message, NotificationCategory category) {
        try {
            notificationFeignClient.sendNotification(SendNotificationRequest.builder()
                    .userId(userId).requestId(requestId).message(message).category(category.name()).build());
        } catch (Exception e) { log.warn("Failed to send notification to userId={}: {}", userId, e.getMessage()); }
    }

    private ResolutionResponse mapToResolutionResponse(Resolution r) {
        return ResolutionResponse.builder()
                .resolutionId(r.getResolutionId()).requestId(r.getRequestId())
                .officerUserId(r.getOfficerUserId()).officerName(r.getOfficerName())
                .actions(r.getActions()).status(r.getStatus())
                .createdAt(r.getCreatedAt()).updatedAt(r.getUpdatedAt()).build();
    }

    private WorkflowStepResponse mapToStepResponse(WorkflowStep s) {
        return WorkflowStepResponse.builder()
                .stepId(s.getStepId()).resolutionId(s.getResolution().getResolutionId())
                .description(s.getDescription()).assignedToUserId(s.getAssignedToUserId())
                .assignedToUserName(s.getAssignedToUserName()).status(s.getStatus())
                .createdAt(s.getCreatedAt()).updatedAt(s.getUpdatedAt()).build();
    }
}

