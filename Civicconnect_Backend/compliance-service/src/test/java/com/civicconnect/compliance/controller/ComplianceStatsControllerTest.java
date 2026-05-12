package com.civicconnect.compliance.controller;

import com.civicconnect.compliance.enums.AuditStatus;
import com.civicconnect.compliance.enums.ComplianceResult;
import com.civicconnect.compliance.repository.AuditRecordRepository;
import com.civicconnect.compliance.repository.ComplianceRecordRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ComplianceStatsController Tests")
class ComplianceStatsControllerTest {

    @Mock private ComplianceRecordRepository complianceRecordRepository;
    @Mock private AuditRecordRepository      auditRecordRepository;

    @InjectMocks
    private ComplianceStatsController controller;

    @Test
    @DisplayName("getStats → 200 with all compliance + audit counts")
    void getStats_returnsAggregated() {
        when(complianceRecordRepository.count()).thenReturn(20L);
        when(complianceRecordRepository.countByResult(ComplianceResult.PASS)).thenReturn(15L);
        when(complianceRecordRepository.countByResult(ComplianceResult.FAIL)).thenReturn(5L);
        when(auditRecordRepository.count()).thenReturn(10L);
        when(auditRecordRepository.countByStatus(AuditStatus.OPEN)).thenReturn(3L);
        when(auditRecordRepository.countByStatus(AuditStatus.IN_REVIEW)).thenReturn(4L);
        when(auditRecordRepository.countByStatus(AuditStatus.CLOSED)).thenReturn(3L);

        ResponseEntity<ComplianceStatsController.ComplianceStatsResponse> resp = controller.getStats();

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        ComplianceStatsController.ComplianceStatsResponse body = resp.getBody();
        assertThat(body.getTotalRecords()).isEqualTo(20L);
        assertThat(body.getPassCount()).isEqualTo(15L);
        assertThat(body.getFailCount()).isEqualTo(5L);
        assertThat(body.getTotalAudits()).isEqualTo(10L);
        assertThat(body.getOpenAudits()).isEqualTo(3L);
        assertThat(body.getInReviewAudits()).isEqualTo(4L);
        assertThat(body.getClosedAudits()).isEqualTo(3L);
    }

    @Test
    @DisplayName("getStats → returns zeros when empty")
    void getStats_emptyState() {
        when(complianceRecordRepository.count()).thenReturn(0L);
        when(auditRecordRepository.count()).thenReturn(0L);

        ResponseEntity<ComplianceStatsController.ComplianceStatsResponse> resp = controller.getStats();

        assertThat(resp.getBody().getTotalRecords()).isZero();
        assertThat(resp.getBody().getTotalAudits()).isZero();
    }
}
