package com.civicconnect.resolution.dto.response;

import com.civicconnect.resolution.enums.WorkflowStepStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class WorkflowStepResponse {

    private Long               stepId;
    private Long               resolutionId;
    private String             description;
    private Long               assignedToUserId;
    private String             assignedToUserName;
    private WorkflowStepStatus status;
    private LocalDateTime      createdAt;
    private LocalDateTime      updatedAt;
}
