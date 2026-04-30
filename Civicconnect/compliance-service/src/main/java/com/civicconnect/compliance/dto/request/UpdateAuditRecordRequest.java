package com.civicconnect.compliance.dto.request;

import com.civicconnect.compliance.enums.AuditStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAuditRecordRequest {

    @NotNull(message = "Status is required")
    private AuditStatus status;   // OPEN → IN_REVIEW → CLOSED

    private String findings;      // Optional — add findings during review
}
