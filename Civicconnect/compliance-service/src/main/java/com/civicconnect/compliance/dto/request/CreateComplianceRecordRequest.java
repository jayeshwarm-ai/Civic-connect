package com.civicconnect.compliance.dto.request;

import com.civicconnect.compliance.enums.ComplianceResult;
import com.civicconnect.compliance.enums.ComplianceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateComplianceRecordRequest {

    @NotNull(message = "Compliance type is required")
    private ComplianceType type;   // REQUEST or RESOLUTION

    @NotNull(message = "Entity ID is required")
    private Long entityId;         // ID of the ServiceRequest or Resolution

    @NotNull(message = "Result is required")
    private ComplianceResult result;  // PASS or FAIL

    @NotBlank(message = "Notes are required")
    private String notes;
}
