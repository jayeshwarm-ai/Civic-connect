package com.civicconnect.resolution.service;

import com.civicconnect.resolution.dto.request.CreateResolutionRequest;
import com.civicconnect.resolution.dto.request.CreateWorkflowStepRequest;
import com.civicconnect.resolution.dto.request.UpdateWorkflowStepRequest;
import com.civicconnect.resolution.dto.response.ResolutionResponse;
import com.civicconnect.resolution.dto.response.WorkflowStepResponse;

import java.util.List;

/**
 * Service interface for Resolution operations.
 */
public interface ResolutionService {
    ResolutionResponse createResolution(CreateResolutionRequest request, Long officerUserId);
    WorkflowStepResponse addWorkflowStep(Long resolutionId, CreateWorkflowStepRequest request, Long officerUserId);
    WorkflowStepResponse updateWorkflowStepStatus(Long stepId, UpdateWorkflowStepRequest request, Long userId);
    ResolutionResponse getResolutionById(Long resolutionId);
    ResolutionResponse getResolutionByRequestId(Long requestId);
    List<WorkflowStepResponse> getWorkflowStepsByResolutionId(Long resolutionId);
    List<ResolutionResponse> getResolutionsByOfficerId(Long officerUserId);
}
