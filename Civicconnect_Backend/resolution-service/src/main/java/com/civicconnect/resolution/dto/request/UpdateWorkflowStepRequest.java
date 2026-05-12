package com.civicconnect.resolution.dto.request;

import com.civicconnect.resolution.enums.WorkflowStepStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateWorkflowStepRequest {

    @NotNull(message = "Status is required")
    private WorkflowStepStatus status;  // Only IN_PROGRESS or COMPLETED allowed
}
