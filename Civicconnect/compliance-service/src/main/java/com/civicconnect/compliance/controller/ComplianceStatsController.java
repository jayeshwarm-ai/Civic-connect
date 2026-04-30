package com.civicconnect.compliance.controller;

import com.civicconnect.compliance.enums.AuditStatus;
import com.civicconnect.compliance.enums.ComplianceResult;
import com.civicconnect.compliance.repository.AuditRecordRepository;
import com.civicconnect.compliance.repository.ComplianceRecordRepository;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Internal-only stats endpoint for reporting-service.
 * NOT in Swagger. NOT behind JWT. Internal cluster access only.
 */
@Hidden
@RestController
@RequestMapping("/internal/compliance/stats")
@RequiredArgsConstructor
public class ComplianceStatsController {

    private final ComplianceRecordRepository complianceRecordRepository;
    private final AuditRecordRepository      auditRecordRepository;

    @GetMapping
    public ResponseEntity<ComplianceStatsResponse> getStats() {
        long totalRecords   = complianceRecordRepository.count();
        long passCount      = complianceRecordRepository.countByResult(ComplianceResult.PASS);
        long failCount      = complianceRecordRepository.countByResult(ComplianceResult.FAIL);
        long totalAudits    = auditRecordRepository.count();
        long openAudits     = auditRecordRepository.countByStatus(AuditStatus.OPEN);
        long inReviewAudits = auditRecordRepository.countByStatus(AuditStatus.IN_REVIEW);
        long closedAudits   = auditRecordRepository.countByStatus(AuditStatus.CLOSED);

        return ResponseEntity.ok(ComplianceStatsResponse.builder()
                .totalRecords(totalRecords)
                .passCount(passCount)
                .failCount(failCount)
                .totalAudits(totalAudits)
                .openAudits(openAudits)
                .inReviewAudits(inReviewAudits)
                .closedAudits(closedAudits)
                .build());
    }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ComplianceStatsResponse {
        private long totalRecords;
        private long passCount;
        private long failCount;
        private long totalAudits;
        private long openAudits;
        private long inReviewAudits;
        private long closedAudits;
    }
}
