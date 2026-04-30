package com.civicconnect.reporting.dto.response;

import com.civicconnect.reporting.enums.ReportScope;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReportResponse {

    private Long          reportId;
    private ReportScope   scope;
    private String        metrics;
    private Long          generatedByUserId;
    private String        generatedByName;
    private LocalDateTime generatedDate;
}
