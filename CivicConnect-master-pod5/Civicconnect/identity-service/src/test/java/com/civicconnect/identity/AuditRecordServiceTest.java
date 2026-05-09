package com.civicconnect.identity;

import com.civicconnect.identity.audit.AuditLogService;
import com.civicconnect.identity.dto.request.CreateAuditRecordRequest;
import com.civicconnect.identity.dto.request.UpdateAuditRecordRequest;
import com.civicconnect.identity.dto.response.AuditRecordResponse;
import com.civicconnect.identity.entity.AuditRecord;
import com.civicconnect.identity.entity.User;
import com.civicconnect.identity.enums.AuditStatus;
import com.civicconnect.identity.enums.Role;
import com.civicconnect.identity.enums.UserStatus;
import com.civicconnect.identity.exception.InvalidOperationException;
import com.civicconnect.identity.exception.ResourceNotFoundException;
import com.civicconnect.identity.repository.AuditRecordRepository;
import com.civicconnect.identity.repository.UserRepository;
import com.civicconnect.identity.service.AuditRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditRecordService Tests")
class AuditRecordServiceTest {

    @Mock private AuditRecordRepository auditRecordRepository;
    @Mock private UserRepository        userRepository;
    @Mock private AuditLogService       auditLogService;

    @InjectMocks
    private AuditRecordService auditRecordService;

    private User complianceOfficer;
    private AuditRecord openRecord;

    @BeforeEach
    void setUp() {
        complianceOfficer = User.builder()
                .userId(20L)
                .name("Officer Compliance")
                .email("compliance@civic.com")
                .phone("9111111111")
                .role(Role.COMPLIANCE_OFFICER)
                .status(UserStatus.ACTIVE)
                .build();

        openRecord = AuditRecord.builder()
                .auditId(1L)
                .officer(complianceOfficer)
                .scope("Road requests Q1 2026")
                .status(AuditStatus.OPEN)
                .build();
    }

    // ── createAuditRecord ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Should create audit record successfully")
    void shouldCreateAuditRecord() {
        CreateAuditRecordRequest request = new CreateAuditRecordRequest();
        request.setScope("Road requests Q1 2026");

        when(userRepository.findById(20L)).thenReturn(Optional.of(complianceOfficer));
        when(auditRecordRepository.save(any(AuditRecord.class))).thenReturn(openRecord);

        AuditRecordResponse response = auditRecordService.createAuditRecord(request, 20L);

        assertThat(response.getScope()).isEqualTo("Road requests Q1 2026");
        assertThat(response.getStatus()).isEqualTo(AuditStatus.OPEN);
        verify(auditLogService).log(eq(20L), any(), eq("AUDIT_RECORD"), anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when officer not found")
    void shouldThrowWhenOfficerNotFound() {
        CreateAuditRecordRequest request = new CreateAuditRecordRequest();
        request.setScope("Test");

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auditRecordService.createAuditRecord(request, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── updateAuditRecord ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Should update audit record status OPEN → IN_REVIEW successfully")
    void shouldUpdateStatusToInReview() {
        UpdateAuditRecordRequest request = new UpdateAuditRecordRequest();
        request.setStatus(AuditStatus.IN_REVIEW);
        request.setFindings("Found 3 overdue requests");

        when(auditRecordRepository.findById(1L)).thenReturn(Optional.of(openRecord));
        when(auditRecordRepository.save(any(AuditRecord.class))).thenReturn(openRecord);

        AuditRecordResponse response = auditRecordService.updateAuditRecord(1L, request, 20L);

        assertThat(response).isNotNull();
        verify(auditRecordRepository).save(any());
    }

    @Test
    @DisplayName("Should throw InvalidOperationException when non-owning officer tries to update")
    void shouldThrowWhenNotOwningOfficer() {
        UpdateAuditRecordRequest request = new UpdateAuditRecordRequest();
        request.setStatus(AuditStatus.IN_REVIEW);

        when(auditRecordRepository.findById(1L)).thenReturn(Optional.of(openRecord));

        // Officer 99 did not create this record (officer 20 did)
        assertThatThrownBy(() -> auditRecordService.updateAuditRecord(1L, request, 99L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Only the officer who created");
    }

    @Test
    @DisplayName("Should throw InvalidOperationException on invalid status transition OPEN → CLOSED")
    void shouldThrowOnInvalidStatusTransition() {
        UpdateAuditRecordRequest request = new UpdateAuditRecordRequest();
        request.setStatus(AuditStatus.CLOSED); // Cannot jump from OPEN to CLOSED

        when(auditRecordRepository.findById(1L)).thenReturn(Optional.of(openRecord));

        assertThatThrownBy(() -> auditRecordService.updateAuditRecord(1L, request, 20L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Invalid audit status transition");
    }

    @Test
    @DisplayName("Should throw InvalidOperationException when trying to update a CLOSED audit")
    void shouldThrowWhenAuditAlreadyClosed() {
        openRecord.setStatus(AuditStatus.CLOSED);
        UpdateAuditRecordRequest request = new UpdateAuditRecordRequest();
        request.setStatus(AuditStatus.IN_REVIEW);

        when(auditRecordRepository.findById(1L)).thenReturn(Optional.of(openRecord));

        assertThatThrownBy(() -> auditRecordService.updateAuditRecord(1L, request, 20L))
                .isInstanceOf(InvalidOperationException.class);
    }

    // ── getById ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return audit record by ID")
    void shouldReturnAuditRecordById() {
        when(auditRecordRepository.findById(1L)).thenReturn(Optional.of(openRecord));

        AuditRecordResponse response = auditRecordService.getById(1L);

        assertThat(response.getAuditId()).isEqualTo(1L);
        assertThat(response.getOfficerId()).isEqualTo(20L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when audit record not found")
    void shouldThrowWhenAuditRecordNotFound() {
        when(auditRecordRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auditRecordService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
