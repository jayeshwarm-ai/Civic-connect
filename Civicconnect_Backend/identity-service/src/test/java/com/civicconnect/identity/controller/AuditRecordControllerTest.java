package com.civicconnect.identity.controller;

import com.civicconnect.identity.dto.request.CreateAuditRecordRequest;
import com.civicconnect.identity.dto.request.UpdateAuditRecordRequest;
import com.civicconnect.identity.dto.response.AuditRecordResponse;
import com.civicconnect.identity.enums.AuditStatus;
import com.civicconnect.identity.service.AuditRecordService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditRecordController Tests")
class AuditRecordControllerTest {

    @Mock private AuditRecordService auditRecordService;

    @InjectMocks
    private AuditRecordController controller;

    private UsernamePasswordAuthenticationToken authWithUserId(Long userId) {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("user", null, Collections.emptyList());
        auth.setDetails(userId);
        return auth;
    }

    private AuditRecordResponse sampleAudit;

    @BeforeEach
    void setUp() {
        sampleAudit = AuditRecordResponse.builder()
                .auditId(1L).officerId(300L).status(AuditStatus.OPEN).build();
    }

    @Test
    @DisplayName("createAuditRecord → 201")
    void createAuditRecord_returns201() {
        CreateAuditRecordRequest req = new CreateAuditRecordRequest();
        when(auditRecordService.createAuditRecord(eq(req), eq(300L))).thenReturn(sampleAudit);

        ResponseEntity<AuditRecordResponse> resp =
                controller.createAuditRecord(req, authWithUserId(300L));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(auditRecordService).createAuditRecord(eq(req), eq(300L));
    }

    @Test
    @DisplayName("updateAuditRecord → 200")
    void updateAuditRecord_returns200() {
        UpdateAuditRecordRequest req = new UpdateAuditRecordRequest();
        when(auditRecordService.updateAuditRecord(eq(1L), eq(req), eq(300L))).thenReturn(sampleAudit);

        ResponseEntity<AuditRecordResponse> resp =
                controller.updateAuditRecord(1L, req, authWithUserId(300L));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(auditRecordService).updateAuditRecord(eq(1L), eq(req), eq(300L));
    }

    @Test
    @DisplayName("getById → 200")
    void getById_returns200() {
        when(auditRecordService.getById(1L)).thenReturn(sampleAudit);

        ResponseEntity<AuditRecordResponse> resp = controller.getById(1L);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getAuditId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getAll → 200")
    void getAll_returns200() {
        when(auditRecordService.getAll()).thenReturn(List.of(sampleAudit));

        ResponseEntity<List<AuditRecordResponse>> resp = controller.getAll();

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("getByOfficer → 200")
    void getByOfficer_returns200() {
        when(auditRecordService.getByOfficer(300L)).thenReturn(List.of(sampleAudit));

        ResponseEntity<List<AuditRecordResponse>> resp = controller.getByOfficer(300L);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("getByStatus → 200")
    void getByStatus_returns200() {
        when(auditRecordService.getByStatus(AuditStatus.OPEN)).thenReturn(Collections.emptyList());

        ResponseEntity<List<AuditRecordResponse>> resp = controller.getByStatus(AuditStatus.OPEN);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isEmpty();
    }
}
