package com.civicconnect.reporting.dto.request;

import com.civicconnect.reporting.enums.ReportScope;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenerateReportRequest {

    @NotNull(message = "Report scope is required")
    private ReportScope scope;  // REQUEST, FEEDBACK, COMPLIANCE, DEPARTMENT
}
