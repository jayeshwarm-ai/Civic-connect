package com.civicconnect.identity.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminResetPasswordRequest {

    @NotNull(message = "Target userId is required")
    private Long userId;
}
