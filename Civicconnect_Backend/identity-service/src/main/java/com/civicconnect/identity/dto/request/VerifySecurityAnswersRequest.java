package com.civicconnect.identity.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class VerifySecurityAnswersRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotNull(message = "Answers are required")
    @Size(min = 3, max = 3, message = "Exactly 3 answers are required")
    @Valid
    private List<SecurityAnswerInput> answers;
}
