package com.civicconnect.compliance.controller;

import com.civicconnect.compliance.dto.request.CreateAuditRecordRequest;
import com.civicconnect.compliance.dto.request.CreateComplianceRecordRequest;
import com.civicconnect.compliance.dto.request.UpdateAuditRecordRequest;
import com.civicconnect.compliance.dto.response.AuditRecordResponse;
import com.civicconnect.compliance.dto.response.ComplianceRecordResponse;
import com.civicconnect.compliance.enums.AuditStatus;
import com.civicconnect.compliance.enums.ComplianceResult;
import com.civicconnect.compliance.enums.ComplianceType;
import com.civicconnect.compliance.service.ComplianceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ComplianceController Tests")
class ComplianceControllerTest {

    @Mock private ComplianceService complianceService;

    @InjectMocks
    private ComplianceController controller;

    private UsernamePasswordAuthenticationToken authWithUserId(Long userId) {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("user", null, Collections.emptyList());
        auth.setDetails(userId);
        return auth;
    }

    private ComplianceRecordResponse sampleRecord;
    private AuditRecordResponse sampleAudit;

    @BeforeEach
    void setUp() {
        sampleRecord = ComplianceRecordResponse.builder()
                .complianceId(1L)
                .entityId(10L)
                .type(ComplianceType.REQUEST)
                .result(ComplianceResult.PASS)
                .build();
        sampleAudit = AuditRecordResponse.builder()
                .auditId(1L)
                .officerUserId(300L)
                .status(AuditStatus.OPEN)
                .build();
    }

    @Test
    @DisplayName("createComplianceRecord → 201")
    void createComplianceRecord_returns201() {
        CreateComplianceRecordRequest req = new CreateComplianceRecordRequest();
        when(complianceService.createComplianceRecord(eq(req), eq(300L))).thenReturn(sampleRecord);

        ResponseEntity<ComplianceRecordResponse> resp =
                controller.createComplianceRecord(req, authWithUserId(300L));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(complianceService).createComplianceRecord(eq(req), eq(300L));
    }

    @Test
    @DisplayName("getComplianceRecordById → 200")
    void getComplianceRecordById_returns200() {
        when(complianceService.getComplianceRecordById(1L)).thenReturn(sampleRecord);

        ResponseEntity<ComplianceRecordResponse> resp = controller.getComplianceRecordById(1L);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getComplianceId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getComplianceRecordsByEntity → 200 with list")
    void getComplianceRecordsByEntity_returns200() {
        when(complianceService.getComplianceRecordsByEntity(ComplianceType.REQUEST, 10L))
                .thenReturn(List.of(sampleRecord));

        ResponseEntity<List<ComplianceRecordResponse>> resp =
                controller.getComplianceRecordsByEntity(ComplianceType.REQUEST, 10L);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("getComplianceRecordsByResult → 200 filtered")
    void getComplianceRecordsByResult_returns200() {
        when(complianceService.getComplianceRecordsByResult(ComplianceResult.FAIL))
                .thenReturn(Collections.emptyList());

        ResponseEntity<List<ComplianceRecordResponse>> resp =
                controller.getComplianceRecordsByResult(ComplianceResult.FAIL);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isEmpty();
    }

    @Test
    @DisplayName("createAuditRecord → 201")
    void createAuditRecord_returns201() {
        CreateAuditRecordRequest req = new CreateAuditRecordRequest();
        when(complianceService.createAuditRecord(eq(req), eq(300L))).thenReturn(sampleAudit);

        ResponseEntity<AuditRecordResponse> resp =
                controller.createAuditRecord(req, authWithUserId(300L));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(complianceService).createAuditRecord(eq(req), eq(300L));
    }

    @Test
    @DisplayName("getAuditRecordById → 200")
    void getAuditRecordById_returns200() {
        when(complianceService.getAuditRecordById(1L)).thenReturn(sampleAudit);

        ResponseEntity<AuditRecordResponse> resp = controller.getAuditRecordById(1L);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getAuditId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("updateAuditRecord → 200")
    void updateAuditRecord_returns200() {
        UpdateAuditRecordRequest req = new UpdateAuditRecordRequest();
        when(complianceService.updateAuditRecord(eq(1L), eq(req), eq(300L))).thenReturn(sampleAudit);

        ResponseEntity<AuditRecordResponse> resp =
                controller.updateAuditRecord(1L, req, authWithUserId(300L));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(complianceService).updateAuditRecord(eq(1L), eq(req), eq(300L));
    }

    @Test
    @DisplayName("getAuditsByOfficerId → 200")
    void getAuditsByOfficerId_returns200() {
        when(complianceService.getAuditsByOfficerId(300L)).thenReturn(List.of(sampleAudit));

        ResponseEntity<List<AuditRecordResponse>> resp = controller.getAuditsByOfficerId(300L);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("getAuditsByStatus → 200")
    void getAuditsByStatus_returns200() {
        when(complianceService.getAuditsByStatus(AuditStatus.OPEN))
                .thenReturn(List.of(sampleAudit));

        ResponseEntity<List<AuditRecordResponse>> resp =
                controller.getAuditsByStatus(AuditStatus.OPEN);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSize(1);
    }
}
