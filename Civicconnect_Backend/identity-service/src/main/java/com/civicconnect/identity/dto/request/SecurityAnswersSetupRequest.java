package com.civicconnect.identity.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Used when a citizen sets up (or updates) their 3 security questions.
 * Sent on registration or from a profile-page action.
 */
@Getter
@Setter
public class SecurityAnswersSetupRequest {

    @NotNull(message = "Security answers are required")
    @Size(min = 3, max = 3, message = "Exactly 3 security answers are required")
    @Valid
    private List<SecurityAnswerInput> answers;
}
