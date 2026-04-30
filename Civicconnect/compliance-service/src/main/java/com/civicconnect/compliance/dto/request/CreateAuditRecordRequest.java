package com.civicconnect.compliance.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAuditRecordRequest {

    @NotBlank(message = "Scope is required")
    private String scope;  // e.g. "Road requests in Jan 2026"
}
