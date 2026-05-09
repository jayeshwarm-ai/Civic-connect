package com.civicconnect.compliance;

import com.civicconnect.compliance.dto.request.CreateAuditRecordRequest;
import com.civicconnect.compliance.dto.request.CreateComplianceRecordRequest;
import com.civicconnect.compliance.dto.request.UpdateAuditRecordRequest;
import com.civicconnect.compliance.dto.response.AuditRecordResponse;
import com.civicconnect.compliance.dto.response.ComplianceRecordResponse;
import com.civicconnect.compliance.entity.AuditRecord;
import com.civicconnect.compliance.entity.ComplianceRecord;
import com.civicconnect.compliance.enums.*;
import com.civicconnect.compliance.exception.InvalidOperationException;
import com.civicconnect.compliance.exception.ResourceNotFoundException;
import com.civicconnect.compliance.feign.*;
import com.civicconnect.compliance.feign.dto.*;
import com.civicconnect.compliance.repository.AuditRecordRepository;
import com.civicconnect.compliance.repository.ComplianceRecordRepository;
import com.civicconnect.compliance.service.ComplianceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ComplianceService Tests")
class ComplianceServiceTest {

    @Mock private ComplianceRecordRepository complianceRecordRepository;
    @Mock private AuditRecordRepository      auditRecordRepository;
    @Mock private IdentityFeignClient        identityFeignClient;
    @Mock private ServiceRequestFeignClient  serviceRequestFeignClient;
    @Mock private ResolutionFeignClient      resolutionFeignClient;
    @Mock private NotificationFeignClient    notificationFeignClient;

    @InjectMocks
    private ComplianceService complianceService;

    private UserValidationResponse complianceOfficer;
    private ComplianceRecord savedRecord;
    private AuditRecord openAudit;

    @BeforeEach
    void setUp() {
        complianceOfficer = UserValidationResponse.builder()
                .userId(300L).name("Officer Carol").email("carol@civic.com")
                .role("COMPLIANCE_OFFICER").status("ACTIVE").exists(true).build();

        savedRecord = ComplianceRecord.builder()
                .complianceId(1L).type(ComplianceType.REQUEST).entityId(10L)
                .createdByUserId(300L).createdByName("Officer Carol")
                .result(ComplianceResult.PASS).notes("All good")
                .createdAt(LocalDateTime.now()).build();

        openAudit = AuditRecord.builder()
                .auditId(1L).officerUserId(300L).officerName("Officer Carol")
                .scope("Road requests Jan 2026").status(AuditStatus.OPEN)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
    }

    // ── createComplianceRecord ─────────────────────────────────────────────────

    @Test
    @DisplayName("Should create PASS compliance record for REQUEST type")
    void shouldCreatePassComplianceRecord() {
        CreateComplianceRecordRequest req = buildComplianceRequest(
                ComplianceType.REQUEST, 10L, ComplianceResult.PASS, "All good");

        when(identityFeignClient.validateUser(300L)).thenReturn(complianceOfficer);
        when(serviceRequestFeignClient.getRequest(10L))
                .thenReturn(ServiceRequestValidationResponse.builder()
                        .requestId(10L).exists(true).build());
        when(complianceRecordRepository.save(any())).thenReturn(savedRecord);
        doNothing().when(identityFeignClient).writeAuditLog(any());

        ComplianceRecordResponse response = complianceService.createComplianceRecord(req, 300L);

        assertThat(response.getComplianceId()).isEqualTo(1L);
        assertThat(response.getResult()).isEqualTo(ComplianceResult.PASS);
        assertThat(response.getType()).isEqualTo(ComplianceType.REQUEST);
        verify(complianceRecordRepository).save(any(ComplianceRecord.class));
        verify(notificationFeignClient, never()).sendNotification(any());
    }

    @Test
    @DisplayName("Should create FAIL compliance record and send notification")
    void shouldCreateFailComplianceRecordAndNotify() {
        CreateComplianceRecordRequest req = buildComplianceRequest(
                ComplianceType.REQUEST, 10L, ComplianceResult.FAIL, "Non-compliant");

        ComplianceRecord failRecord = ComplianceRecord.builder()
                .complianceId(2L).type(ComplianceType.REQUEST).entityId(10L)
                .createdByUserId(300L).createdByName("Officer Carol")
                .result(ComplianceResult.FAIL).notes("Non-compliant")
                .createdAt(LocalDateTime.now()).build();

        when(identityFeignClient.validateUser(300L)).thenReturn(complianceOfficer);
        when(serviceRequestFeignClient.getRequest(10L))
                .thenReturn(ServiceRequestValidationResponse.builder()
                        .requestId(10L).exists(true).build());
        when(complianceRecordRepository.save(any())).thenReturn(failRecord);
        doNothing().when(identityFeignClient).writeAuditLog(any());
        doNothing().when(notificationFeignClient).sendNotification(any());

        ComplianceRecordResponse response = complianceService.createComplianceRecord(req, 300L);

        assertThat(response.getResult()).isEqualTo(ComplianceResult.FAIL);
        verify(notificationFeignClient).sendNotification(any(SendNotificationRequest.class));
    }

    @Test
    @DisplayName("Should create compliance record for RESOLUTION type")
    void shouldCreateComplianceRecordForResolution() {
        CreateComplianceRecordRequest req = buildComplianceRequest(
                ComplianceType.RESOLUTION, 5L, ComplianceResult.PASS, "Resolution OK");

        ComplianceRecord resRecord = ComplianceRecord.builder()
                .complianceId(3L).type(ComplianceType.RESOLUTION).entityId(5L)
                .createdByUserId(300L).createdByName("Officer Carol")
                .result(ComplianceResult.PASS).notes("Resolution OK")
                .createdAt(LocalDateTime.now()).build();

        when(identityFeignClient.validateUser(300L)).thenReturn(complianceOfficer);
        when(resolutionFeignClient.getResolution(5L))
                .thenReturn(ResolutionValidationResponse.builder()
                        .resolutionId(5L).exists(true).build());
        when(complianceRecordRepository.save(any())).thenReturn(resRecord);
        doNothing().when(identityFeignClient).writeAuditLog(any());

        ComplianceRecordResponse response = complianceService.createComplianceRecord(req, 300L);

        assertThat(response.getType()).isEqualTo(ComplianceType.RESOLUTION);
        verify(resolutionFeignClient).getResolution(5L);
        verify(serviceRequestFeignClient, never()).getRequest(anyLong());
    }

    @Test
    @DisplayName("Should throw InvalidOperationException when not COMPLIANCE_OFFICER")
    void shouldThrowWhenNotComplianceOfficer() {
        UserValidationResponse serviceOfficer = UserValidationResponse.builder()
                .userId(200L).role("SERVICE_OFFICER").exists(true).build();

        when(identityFeignClient.validateUser(200L)).thenReturn(serviceOfficer);

        CreateComplianceRecordRequest req = buildComplianceRequest(
                ComplianceType.REQUEST, 10L, ComplianceResult.PASS, "Test");

        assertThatThrownBy(() -> complianceService.createComplianceRecord(req, 200L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Only COMPLIANCE_OFFICERs");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when ServiceRequest not found")
    void shouldThrowWhenServiceRequestNotFound() {
        when(identityFeignClient.validateUser(300L)).thenReturn(complianceOfficer);
        when(serviceRequestFeignClient.getRequest(99L))
                .thenReturn(ServiceRequestValidationResponse.builder()
                        .requestId(99L).exists(false).build());

        CreateComplianceRecordRequest req = buildComplianceRequest(
                ComplianceType.REQUEST, 99L, ComplianceResult.PASS, "Test");

        assertThatThrownBy(() -> complianceService.createComplianceRecord(req, 300L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ServiceRequest not found");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when Resolution not found")
    void shouldThrowWhenResolutionNotFound() {
        when(identityFeignClient.validateUser(300L)).thenReturn(complianceOfficer);
        when(resolutionFeignClient.getResolution(99L))
                .thenReturn(ResolutionValidationResponse.builder()
                        .resolutionId(99L).exists(false).build());

        CreateComplianceRecordRequest req = buildComplianceRequest(
                ComplianceType.RESOLUTION, 99L, ComplianceResult.PASS, "Test");

        assertThatThrownBy(() -> complianceService.createComplianceRecord(req, 300L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Resolution not found");
    }

    // ── createAuditRecord ──────────────────────────────────────────────────────

    @Test
    @DisplayName("Should create audit record with OPEN status")
    void shouldCreateAuditRecord() {
        CreateAuditRecordRequest req = new CreateAuditRecordRequest();
        req.setScope("Road requests Jan 2026");

        when(identityFeignClient.validateUser(300L)).thenReturn(complianceOfficer);
        when(auditRecordRepository.save(any())).thenReturn(openAudit);
        doNothing().when(identityFeignClient).writeAuditLog(any());

        AuditRecordResponse response = complianceService.createAuditRecord(req, 300L);

        assertThat(response.getAuditId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(AuditStatus.OPEN);
        assertThat(response.getScope()).isEqualTo("Road requests Jan 2026");
        verify(auditRecordRepository).save(any(AuditRecord.class));
    }

    // ── updateAuditRecord ──────────────────────────────────────────────────────

    @Test
    @DisplayName("Should update audit OPEN → IN_REVIEW successfully")
    void shouldUpdateAuditToInReview() {
        UpdateAuditRecordRequest req = new UpdateAuditRecordRequest();
        req.setStatus(AuditStatus.IN_REVIEW);
        req.setFindings("Found 3 issues");

        when(auditRecordRepository.findById(1L)).thenReturn(Optional.of(openAudit));
        when(auditRecordRepository.save(any())).thenReturn(openAudit);
        doNothing().when(identityFeignClient).writeAuditLog(any());

        AuditRecordResponse response = complianceService.updateAuditRecord(1L, req, 300L);

        assertThat(response).isNotNull();
        verify(auditRecordRepository).save(any(AuditRecord.class));
    }

    @Test
    @DisplayName("Should throw InvalidOperationException on invalid transition OPEN → CLOSED")
    void shouldThrowOnInvalidAuditTransition() {
        UpdateAuditRecordRequest req = new UpdateAuditRecordRequest();
        req.setStatus(AuditStatus.CLOSED);   // Must go through IN_REVIEW first

        when(auditRecordRepository.findById(1L)).thenReturn(Optional.of(openAudit));

        assertThatThrownBy(() -> complianceService.updateAuditRecord(1L, req, 300L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Invalid audit status transition");
    }

    @Test
    @DisplayName("Should throw InvalidOperationException when wrong officer updates audit")
    void shouldThrowWhenWrongOfficerUpdates() {
        UpdateAuditRecordRequest req = new UpdateAuditRecordRequest();
        req.setStatus(AuditStatus.IN_REVIEW);

        when(auditRecordRepository.findById(1L)).thenReturn(Optional.of(openAudit));

        // Officer 999 didn't create this audit (300 did)
        assertThatThrownBy(() -> complianceService.updateAuditRecord(1L, req, 999L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("officer who created");
    }

    @Test
    @DisplayName("Should throw InvalidOperationException when updating CLOSED audit")
    void shouldThrowWhenUpdatingClosedAudit() {
        AuditRecord closedAudit = AuditRecord.builder()
                .auditId(1L).officerUserId(300L).status(AuditStatus.CLOSED)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        UpdateAuditRecordRequest req = new UpdateAuditRecordRequest();
        req.setStatus(AuditStatus.IN_REVIEW);

        when(auditRecordRepository.findById(1L)).thenReturn(Optional.of(closedAudit));

        assertThatThrownBy(() -> complianceService.updateAuditRecord(1L, req, 300L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("CLOSED audit");
    }

    @Test
    @DisplayName("Should return audit records by status")
    void shouldReturnAuditsByStatus() {
        when(auditRecordRepository.findByStatusOrderByCreatedAtDesc(AuditStatus.OPEN))
                .thenReturn(List.of(openAudit));

        List<AuditRecordResponse> results =
                complianceService.getAuditsByStatus(AuditStatus.OPEN);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getStatus()).isEqualTo(AuditStatus.OPEN);
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    private CreateComplianceRecordRequest buildComplianceRequest(
            ComplianceType type, Long entityId, ComplianceResult result, String notes) {
        CreateComplianceRecordRequest req = new CreateComplianceRecordRequest();
        req.setType(type);
        req.setEntityId(entityId);
        req.setResult(result);
        req.setNotes(notes);
        return req;
    }
}
