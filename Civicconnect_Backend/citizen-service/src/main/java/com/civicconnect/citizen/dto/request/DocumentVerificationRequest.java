package com.civicconnect.citizen.dto.request;

import com.civicconnect.citizen.enums.VerificationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentVerificationRequest {

    @NotNull(message = "Verification status is required")
    private VerificationStatus verificationStatus;   // VERIFIED or REJECTED only

    private String remarks;   // Required when verificationStatus = REJECTED
}
