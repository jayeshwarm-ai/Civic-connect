package com.civicconnect.resolution.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateWorkflowStepRequest {

    @NotBlank(message = "Step description is required")
    private String description;

    @NotNull(message = "Assigned user ID is required")
    private Long assignedToUserId;
}
