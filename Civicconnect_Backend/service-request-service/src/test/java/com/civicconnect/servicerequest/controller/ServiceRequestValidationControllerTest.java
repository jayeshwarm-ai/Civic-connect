package com.civicconnect.servicerequest.controller;

import com.civicconnect.servicerequest.dto.request.StatusUpdateInternalRequest;
import com.civicconnect.servicerequest.dto.response.ServiceRequestValidationResponse;
import com.civicconnect.servicerequest.entity.RequestUpdate;
import com.civicconnect.servicerequest.entity.ServiceRequest;
import com.civicconnect.servicerequest.enums.ServiceRequestStatus;
import com.civicconnect.servicerequest.enums.ServiceRequestType;
import com.civicconnect.servicerequest.repository.RequestUpdateRepository;
import com.civicconnect.servicerequest.repository.ServiceRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Covers the internal validation endpoint and the audit-row-writing logic
 * that was added so resolution-driven status changes show up in citizen
 * update history.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ServiceRequestValidationController Tests")
class ServiceRequestValidationControllerTest {

    @Mock private ServiceRequestRepository serviceRequestRepository;
    @Mock private RequestUpdateRepository  requestUpdateRepository;

    @InjectMocks
    private ServiceRequestValidationController controller;

    private ServiceRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleRequest = ServiceRequest.builder()
                .requestId(1L)
                .citizenId(50L)
                .citizenUserId(100L)
                .assignedOfficerUserId(200L)
                .type(ServiceRequestType.ROAD)
                .status(ServiceRequestStatus.ASSIGNED)
                .build();
    }

    // ── GET /internal/service-requests/{requestId} ────────────────────────────

    @Test
    @DisplayName("getRequest → 200 with exists=true when found")
    void getRequest_existingId() {
        when(serviceRequestRepository.findById(1L)).thenReturn(Optional.of(sampleRequest));

        ResponseEntity<ServiceRequestValidationResponse> resp = controller.getRequest(1L);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getRequestId()).isEqualTo(1L);
        assertThat(resp.getBody().isExists()).isTrue();
    }

    @Test
    @DisplayName("getRequest → 200 with exists=false when not found")
    void getRequest_missingId() {
        when(serviceRequestRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<ServiceRequestValidationResponse> resp = controller.getRequest(99L);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().isExists()).isFalse();
        assertThat(resp.getBody().getRequestId()).isEqualTo(99L);
    }

    // ── POST /internal/service-requests/{requestId}/status ────────────────────

    @Test
    @DisplayName("updateStatus → flips status and writes audit row when actor present")
    void updateStatus_writesAuditRow() {
        when(serviceRequestRepository.findById(1L)).thenReturn(Optional.of(sampleRequest));

        StatusUpdateInternalRequest req = new StatusUpdateInternalRequest();
        req.setStatus(ServiceRequestStatus.IN_PROGRESS);
        req.setOfficerUserId(200L);
        req.setOfficerName("John (Service Officer)");
        req.setNotes("Resolution #1 opened.");

        ResponseEntity<Void> resp = controller.updateStatus(1L, req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(sampleRequest.getStatus()).isEqualTo(ServiceRequestStatus.IN_PROGRESS);
        verify(serviceRequestRepository).save(sampleRequest);

        ArgumentCaptor<RequestUpdate> captor = ArgumentCaptor.forClass(RequestUpdate.class);
        verify(requestUpdateRepository).save(captor.capture());
        RequestUpdate audit = captor.getValue();
        assertThat(audit.getOfficerUserId()).isEqualTo(200L);
        assertThat(audit.getOfficerName()).isEqualTo("John (Service Officer)");
        assertThat(audit.getNotes()).isEqualTo("Resolution #1 opened.");
        assertThat(audit.getStatus()).isEqualTo(ServiceRequestStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("updateStatus → skips audit row when officerUserId missing (defensive)")
    void updateStatus_skipsAuditWithoutActor() {
        when(serviceRequestRepository.findById(1L)).thenReturn(Optional.of(sampleRequest));

        StatusUpdateInternalRequest req = new StatusUpdateInternalRequest();
        req.setStatus(ServiceRequestStatus.RESOLVED);
        // No actor info — should NOT write an audit row (notes column is NOT NULL)

        controller.updateStatus(1L, req);

        verify(serviceRequestRepository).save(sampleRequest);
        verify(requestUpdateRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateStatus → falls back to 'Officer#<id>' when officerName missing")
    void updateStatus_fallbackOfficerName() {
        when(serviceRequestRepository.findById(1L)).thenReturn(Optional.of(sampleRequest));

        StatusUpdateInternalRequest req = new StatusUpdateInternalRequest();
        req.setStatus(ServiceRequestStatus.IN_PROGRESS);
        req.setOfficerUserId(200L);
        req.setNotes("Some notes");
        // officerName intentionally null

        controller.updateStatus(1L, req);

        ArgumentCaptor<RequestUpdate> captor = ArgumentCaptor.forClass(RequestUpdate.class);
        verify(requestUpdateRepository).save(captor.capture());
        assertThat(captor.getValue().getOfficerName()).isEqualTo("Officer#200");
    }

    @Test
    @DisplayName("updateStatus → falls back to default notes when notes is blank")
    void updateStatus_fallbackNotes() {
        when(serviceRequestRepository.findById(1L)).thenReturn(Optional.of(sampleRequest));

        StatusUpdateInternalRequest req = new StatusUpdateInternalRequest();
        req.setStatus(ServiceRequestStatus.RESOLVED);
        req.setOfficerUserId(200L);
        req.setOfficerName("John");
        req.setNotes("   "); // blank

        controller.updateStatus(1L, req);

        ArgumentCaptor<RequestUpdate> captor = ArgumentCaptor.forClass(RequestUpdate.class);
        verify(requestUpdateRepository).save(captor.capture());
        assertThat(captor.getValue().getNotes()).isEqualTo("Status changed to RESOLVED");
    }

    @Test
    @DisplayName("updateStatus → no-op when requestId not found")
    void updateStatus_missingId() {
        when(serviceRequestRepository.findById(99L)).thenReturn(Optional.empty());

        StatusUpdateInternalRequest req = new StatusUpdateInternalRequest();
        req.setStatus(ServiceRequestStatus.RESOLVED);

        ResponseEntity<Void> resp = controller.updateStatus(99L, req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(serviceRequestRepository, never()).save(any());
        verify(requestUpdateRepository, never()).save(any());
    }
}
