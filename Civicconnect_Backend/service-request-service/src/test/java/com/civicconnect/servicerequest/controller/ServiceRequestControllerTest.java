package com.civicconnect.servicerequest.controller;

import com.civicconnect.servicerequest.dto.request.AssignOfficerRequest;
import com.civicconnect.servicerequest.dto.request.RequestStatusUpdateRequest;
import com.civicconnect.servicerequest.dto.request.ServiceRequestSubmitRequest;
import com.civicconnect.servicerequest.dto.response.RequestUpdateResponse;
import com.civicconnect.servicerequest.dto.response.ServiceRequestResponse;
import com.civicconnect.servicerequest.enums.ServiceRequestStatus;
import com.civicconnect.servicerequest.enums.ServiceRequestType;
import com.civicconnect.servicerequest.service.ServiceRequestService;
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
@DisplayName("ServiceRequestController Tests")
class ServiceRequestControllerTest {

    @Mock private ServiceRequestService serviceRequestService;

    @InjectMocks
    private ServiceRequestController controller;

    private UsernamePasswordAuthenticationToken authWithUserId(Long userId) {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("user", null, Collections.emptyList());
        auth.setDetails(userId);
        return auth;
    }

    private ServiceRequestResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleResponse = ServiceRequestResponse.builder()
                .requestId(1L)
                .citizenId(50L)
                .type(ServiceRequestType.ROAD)
                .status(ServiceRequestStatus.SUBMITTED)
                .description("Pothole")
                .build();
    }

    @Test
    @DisplayName("submitRequest → 201, delegates with extracted userId")
    void submitRequest_returns201() {
        ServiceRequestSubmitRequest req = new ServiceRequestSubmitRequest();
        when(serviceRequestService.submitRequest(eq(100L), eq(req))).thenReturn(sampleResponse);

        ResponseEntity<ServiceRequestResponse> resp =
                controller.submitRequest(req, authWithUserId(100L));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody()).isEqualTo(sampleResponse);
        verify(serviceRequestService).submitRequest(eq(100L), eq(req));
    }

    @Test
    @DisplayName("closeRequest → 200")
    void closeRequest_returns200() {
        when(serviceRequestService.closeRequest(eq(1L), eq(100L))).thenReturn(sampleResponse);

        ResponseEntity<ServiceRequestResponse> resp =
                controller.closeRequest(1L, authWithUserId(100L));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(serviceRequestService).closeRequest(eq(1L), eq(100L));
    }

    @Test
    @DisplayName("withdrawRequest → 204 with no body")
    void withdrawRequest_returns204() {
        doNothing().when(serviceRequestService).withdrawRequest(eq(1L), eq(100L));

        ResponseEntity<Void> resp = controller.withdrawRequest(1L, authWithUserId(100L));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(resp.getBody()).isNull();
        verify(serviceRequestService).withdrawRequest(eq(1L), eq(100L));
    }

    @Test
    @DisplayName("updateRequest → 200")
    void updateRequest_returns200() {
        ServiceRequestSubmitRequest req = new ServiceRequestSubmitRequest();
        when(serviceRequestService.updateRequest(eq(1L), eq(100L), eq(req))).thenReturn(sampleResponse);

        ResponseEntity<ServiceRequestResponse> resp =
                controller.updateRequest(1L, req, authWithUserId(100L));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(serviceRequestService).updateRequest(eq(1L), eq(100L), eq(req));
    }

    @Test
    @DisplayName("getMyRequests → 200 with the user's request list")
    void getMyRequests_returns200() {
        when(serviceRequestService.getMyRequests(100L)).thenReturn(List.of(sampleResponse));

        ResponseEntity<List<ServiceRequestResponse>> resp =
                controller.getMyRequests(authWithUserId(100L));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("getRequestById → 200, forwards authorities to service")
    void getRequestById_returns200() {
        UsernamePasswordAuthenticationToken auth = authWithUserId(100L);
        when(serviceRequestService.getRequestById(eq(1L), eq(100L), eq(auth.getAuthorities())))
                .thenReturn(sampleResponse);

        ResponseEntity<ServiceRequestResponse> resp = controller.getRequestById(1L, auth);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(serviceRequestService).getRequestById(eq(1L), eq(100L), eq(auth.getAuthorities()));
    }

    @Test
    @DisplayName("getRequestsByCitizenId → 200 with list")
    void getRequestsByCitizenId_returns200() {
        when(serviceRequestService.getRequestsByCitizenId(50L))
                .thenReturn(List.of(sampleResponse));

        ResponseEntity<List<ServiceRequestResponse>> resp =
                controller.getRequestsByCitizenId(50L);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("getRequestUpdates → 200 with update history")
    void getRequestUpdates_returns200() {
        UsernamePasswordAuthenticationToken auth = authWithUserId(100L);
        RequestUpdateResponse update = RequestUpdateResponse.builder()
                .updateId(1L).requestId(1L).status(ServiceRequestStatus.SUBMITTED).build();
        when(serviceRequestService.getRequestUpdates(eq(1L), eq(100L), eq(auth.getAuthorities())))
                .thenReturn(List.of(update));

        ResponseEntity<List<RequestUpdateResponse>> resp = controller.getRequestUpdates(1L, auth);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("getRequestsByOfficerId → 200 with officer's list")
    void getRequestsByOfficerId_returns200() {
        when(serviceRequestService.getRequestsByOfficerId(200L))
                .thenReturn(List.of(sampleResponse));

        ResponseEntity<List<ServiceRequestResponse>> resp =
                controller.getRequestsByOfficerId(200L);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("getRequestsByStatus → 200 filtered by status")
    void getRequestsByStatus_returns200() {
        UsernamePasswordAuthenticationToken auth = authWithUserId(200L);
        when(serviceRequestService.getRequestsByStatus(
                eq(ServiceRequestStatus.ASSIGNED), eq(200L), eq(auth.getAuthorities())))
                .thenReturn(List.of(sampleResponse));

        ResponseEntity<List<ServiceRequestResponse>> resp =
                controller.getRequestsByStatus(ServiceRequestStatus.ASSIGNED, auth);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("getRequestsByStatus → 200 with empty list when none match")
    void getRequestsByStatus_emptyList() {
        UsernamePasswordAuthenticationToken auth = authWithUserId(200L);
        when(serviceRequestService.getRequestsByStatus(any(), anyLong(), any()))
                .thenReturn(Collections.emptyList());

        ResponseEntity<List<ServiceRequestResponse>> resp =
                controller.getRequestsByStatus(ServiceRequestStatus.RESOLVED, auth);

        assertThat(resp.getBody()).isEmpty();
    }

    @Test
    @DisplayName("updateRequestStatus → 200")
    void updateRequestStatus_returns200() {
        RequestStatusUpdateRequest req = new RequestStatusUpdateRequest();
        when(serviceRequestService.updateRequestStatus(eq(1L), eq(req), eq(200L)))
                .thenReturn(sampleResponse);

        ResponseEntity<ServiceRequestResponse> resp =
                controller.updateRequestStatus(1L, req, authWithUserId(200L));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(serviceRequestService).updateRequestStatus(eq(1L), eq(req), eq(200L));
    }

    @Test
    @DisplayName("assignOfficer → 200")
    void assignOfficer_returns200() {
        AssignOfficerRequest req = new AssignOfficerRequest();
        when(serviceRequestService.assignOfficer(eq(1L), eq(req), eq(1L)))
                .thenReturn(sampleResponse);

        ResponseEntity<ServiceRequestResponse> resp =
                controller.assignOfficer(1L, req, authWithUserId(1L));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(serviceRequestService).assignOfficer(eq(1L), eq(req), eq(1L));
    }
}
