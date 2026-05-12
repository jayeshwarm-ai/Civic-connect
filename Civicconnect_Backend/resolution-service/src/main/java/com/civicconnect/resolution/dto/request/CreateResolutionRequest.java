package com.civicconnect.resolution.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateResolutionRequest {

    @NotNull(message = "Request ID is required")
    private Long requestId;

    @NotBlank(message = "Actions description is required")
    private String actions;
}
