package com.civicconnect.reporting.feign.dto;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ComplianceStatsResponse {
    private long totalRecords, passCount, failCount;
    private long totalAudits, openAudits, inReviewAudits, closedAudits;
}
