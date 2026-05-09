package com.civicconnect.servicerequest;

import com.civicconnect.servicerequest.dto.request.AssignOfficerRequest;
import com.civicconnect.servicerequest.dto.request.RequestStatusUpdateRequest;
import com.civicconnect.servicerequest.dto.request.ServiceRequestSubmitRequest;
import com.civicconnect.servicerequest.dto.response.RequestUpdateResponse;
import com.civicconnect.servicerequest.dto.response.ServiceRequestResponse;
import com.civicconnect.servicerequest.entity.RequestUpdate;
import com.civicconnect.servicerequest.entity.ServiceRequest;
import com.civicconnect.servicerequest.enums.ServiceRequestStatus;
import com.civicconnect.servicerequest.enums.ServiceRequestType;
import com.civicconnect.servicerequest.exception.InvalidOperationException;
import com.civicconnect.servicerequest.exception.ResourceNotFoundException;
import com.civicconnect.servicerequest.feign.CitizenFeignClient;
import com.civicconnect.servicerequest.feign.IdentityFeignClient;
import com.civicconnect.servicerequest.feign.NotificationFeignClient;
import com.civicconnect.servicerequest.feign.dto.CitizenValidationResponse;
import com.civicconnect.servicerequest.feign.dto.UserValidationResponse;
import com.civicconnect.servicerequest.repository.RequestUpdateRepository;
import com.civicconnect.servicerequest.repository.ServiceRequestRepository;
import com.civicconnect.servicerequest.service.ServiceRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ServiceRequestService Tests")
class ServiceRequestServiceTest {

    @Mock private ServiceRequestRepository serviceRequestRepository;
    @Mock private RequestUpdateRepository  requestUpdateRepository;
    @Mock private CitizenFeignClient       citizenFeignClient;
    @Mock private IdentityFeignClient      identityFeignClient;
    @Mock private NotificationFeignClient  notificationFeignClient;

    @InjectMocks
    private ServiceRequestService serviceRequestService;

    private ServiceRequest submittedRequest;
    private ServiceRequest assignedRequest;

    @BeforeEach
    void setUp() {
        submittedRequest = ServiceRequest.builder()
                .requestId(1L)
                .citizenId(10L)
                .citizenName("Alice Citizen")
                .citizenUserId(100L)
                .type(ServiceRequestType.ROAD)
                .description("Pothole on Main St")
                .location("Main St, Block 4")
                .status(ServiceRequestStatus.SUBMITTED)
                .build();

        assignedRequest = ServiceRequest.builder()
                .requestId(2L)
                .citizenId(10L)
                .citizenName("Alice Citizen")
                .citizenUserId(100L)
                .assignedOfficerUserId(200L)
                .assignedOfficerName("Officer Bob")
                .type(ServiceRequestType.WATER)
                .description("Water leakage")
                .location("East St")
                .status(ServiceRequestStatus.ASSIGNED)
                .build();
    }

    // ── submitRequest ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should submit request successfully for ACTIVE citizen")
    void shouldSubmitRequestSuccessfully() {
        ServiceRequestSubmitRequest request = new ServiceRequestSubmitRequest();
        request.setType(ServiceRequestType.ROAD);
        request.setDescription("Pothole");
        request.setLocation("Main St");

        when(citizenFeignClient.getCitizenByUserId(100L))
                .thenReturn(CitizenValidationResponse.builder()
                        .citizenId(10L).name("Alice").accountStatus("ACTIVE").exists(true).build());
        when(serviceRequestRepository.save(any())).thenReturn(submittedRequest);
        doNothing().when(notificationFeignClient).sendNotification(any());
        doNothing().when(identityFeignClient).writeAuditLog(any());

        ServiceRequestResponse response = serviceRequestService.submitRequest(100L, request);

        assertThat(response.getRequestId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(ServiceRequestStatus.SUBMITTED);
        verify(serviceRequestRepository).save(any(ServiceRequest.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when citizen not found on submit")
    void shouldThrowWhenCitizenNotFoundOnSubmit() {
        ServiceRequestSubmitRequest request = new ServiceRequestSubmitRequest();
        request.setType(ServiceRequestType.ROAD);
        request.setDescription("Pothole");
        request.setLocation("Main St");

        when(citizenFeignClient.getCitizenByUserId(999L))
                .thenReturn(CitizenValidationResponse.builder().userId(999L).exists(false).build());

        assertThatThrownBy(() -> serviceRequestService.submitRequest(999L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Citizen profile not found");
    }

    @Test
    @DisplayName("Should throw InvalidOperationException when citizen account is INACTIVE")
    void shouldThrowWhenCitizenInactive() {
        ServiceRequestSubmitRequest request = new ServiceRequestSubmitRequest();
        request.setType(ServiceRequestType.ROAD);
        request.setDescription("Pothole");
        request.setLocation("Main St");

        when(citizenFeignClient.getCitizenByUserId(100L))
                .thenReturn(CitizenValidationResponse.builder()
                        .citizenId(10L).accountStatus("INACTIVE").exists(true).build());

        assertThatThrownBy(() -> serviceRequestService.submitRequest(100L, request))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("ACTIVE accounts");
    }

    // ── assignOfficer ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should assign officer to SUBMITTED request successfully")
    void shouldAssignOfficerSuccessfully() {
        AssignOfficerRequest request = new AssignOfficerRequest();
        request.setOfficerId(200L);

        when(serviceRequestRepository.findById(1L)).thenReturn(Optional.of(submittedRequest));
        when(identityFeignClient.validateUser(200L))
                .thenReturn(UserValidationResponse.builder()
                        .userId(200L).name("Officer Bob").role("SERVICE_OFFICER").exists(true).build());
        when(serviceRequestRepository.save(any())).thenReturn(submittedRequest);
        when(requestUpdateRepository.save(any())).thenReturn(new RequestUpdate());
        doNothing().when(identityFeignClient).writeAuditLog(any());
        doNothing().when(notificationFeignClient).sendNotification(any());

        ServiceRequestResponse response = serviceRequestService.assignOfficer(1L, request, 999L);

        assertThat(response).isNotNull();
        verify(requestUpdateRepository).save(any(RequestUpdate.class));
    }

    @Test
    @DisplayName("Should throw InvalidOperationException when assigning to non-SUBMITTED request")
    void shouldThrowWhenAssigningToNonSubmittedRequest() {
        AssignOfficerRequest request = new AssignOfficerRequest();
        request.setOfficerId(200L);

        when(serviceRequestRepository.findById(2L)).thenReturn(Optional.of(assignedRequest));

        assertThatThrownBy(() -> serviceRequestService.assignOfficer(2L, request, 999L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Only SUBMITTED requests can be assigned");
    }

    @Test
    @DisplayName("Should throw InvalidOperationException when user is not SERVICE_OFFICER")
    void shouldThrowWhenUserIsNotOfficer() {
        AssignOfficerRequest request = new AssignOfficerRequest();
        request.setOfficerId(300L);

        when(serviceRequestRepository.findById(1L)).thenReturn(Optional.of(submittedRequest));
        when(identityFeignClient.validateUser(300L))
                .thenReturn(UserValidationResponse.builder()
                        .userId(300L).role("CITIZEN").exists(true).build());

        assertThatThrownBy(() -> serviceRequestService.assignOfficer(1L, request, 999L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("not a SERVICE_OFFICER");
    }

    // ── updateRequestStatus ───────────────────────────────────────────────────

    @Test
    @DisplayName("Should update status ASSIGNED → IN_PROGRESS successfully")
    void shouldUpdateStatusToInProgress() {
        RequestStatusUpdateRequest request = new RequestStatusUpdateRequest();
        request.setStatus(ServiceRequestStatus.IN_PROGRESS);
        request.setNotes("Started working on pothole");

        when(serviceRequestRepository.findById(2L)).thenReturn(Optional.of(assignedRequest));
        when(identityFeignClient.validateUser(200L))
                .thenReturn(UserValidationResponse.builder()
                        .userId(200L).name("Officer Bob").exists(true).build());
        when(serviceRequestRepository.save(any())).thenReturn(assignedRequest);
        when(requestUpdateRepository.save(any())).thenReturn(new RequestUpdate());
        doNothing().when(identityFeignClient).writeAuditLog(any());
        doNothing().when(notificationFeignClient).sendNotification(any());

        ServiceRequestResponse response =
                serviceRequestService.updateRequestStatus(2L, request, 200L);

        assertThat(response).isNotNull();
        verify(requestUpdateRepository).save(any(RequestUpdate.class));
    }

    @Test
    @DisplayName("Should throw InvalidOperationException when wrong officer tries to update")
    void shouldThrowWhenWrongOfficerUpdates() {
        RequestStatusUpdateRequest request = new RequestStatusUpdateRequest();
        request.setStatus(ServiceRequestStatus.IN_PROGRESS);
        request.setNotes("test");

        when(serviceRequestRepository.findById(2L)).thenReturn(Optional.of(assignedRequest));

        // Officer 999 is not assigned (assigned officer is 200)
        assertThatThrownBy(() -> serviceRequestService.updateRequestStatus(2L, request, 999L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("not the assigned officer");
    }

    @Test
    @DisplayName("Should throw InvalidOperationException on invalid status transition IN_PROGRESS → ASSIGNED")
    void shouldThrowOnInvalidStatusTransition() {
        assignedRequest.setStatus(ServiceRequestStatus.IN_PROGRESS);
        RequestStatusUpdateRequest request = new RequestStatusUpdateRequest();
        request.setStatus(ServiceRequestStatus.ASSIGNED); // backward transition
        request.setNotes("test");

        when(serviceRequestRepository.findById(2L)).thenReturn(Optional.of(assignedRequest));

        assertThatThrownBy(() -> serviceRequestService.updateRequestStatus(2L, request, 200L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Officer can only update status to IN_PROGRESS or RESOLVED");
    }

    // ── closeRequest ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should close RESOLVED request successfully")
    void shouldCloseResolvedRequest() {
        submittedRequest.setStatus(ServiceRequestStatus.RESOLVED);
        when(serviceRequestRepository.findById(1L)).thenReturn(Optional.of(submittedRequest));
        when(serviceRequestRepository.save(any())).thenReturn(submittedRequest);
        doNothing().when(notificationFeignClient).sendNotification(any());
        doNothing().when(identityFeignClient).writeAuditLog(any());

        ServiceRequestResponse response = serviceRequestService.closeRequest(1L, 100L);

        assertThat(response).isNotNull();
        verify(serviceRequestRepository).save(any(ServiceRequest.class));
    }

    @Test
    @DisplayName("Should throw InvalidOperationException when closing non-RESOLVED request")
    void shouldThrowWhenClosingNonResolvedRequest() {
        when(serviceRequestRepository.findById(1L)).thenReturn(Optional.of(submittedRequest));

        assertThatThrownBy(() -> serviceRequestService.closeRequest(1L, 100L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("RESOLVED");
    }

    @Test
    @DisplayName("Should throw InvalidOperationException when wrong citizen tries to close")
    void shouldThrowWhenWrongCitizenCloses() {
        submittedRequest.setStatus(ServiceRequestStatus.RESOLVED);
        when(serviceRequestRepository.findById(1L)).thenReturn(Optional.of(submittedRequest));

        // citizenUserId 999 is not the owner (owner is 100)
        assertThatThrownBy(() -> serviceRequestService.closeRequest(1L, 999L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("not authorized");
    }

    // ── withdrawRequest ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Should withdraw SUBMITTED request successfully")
    void shouldWithdrawSubmittedRequest() {
        when(serviceRequestRepository.findById(1L)).thenReturn(Optional.of(submittedRequest));
        doNothing().when(serviceRequestRepository).delete(submittedRequest);
        doNothing().when(identityFeignClient).writeAuditLog(any());

        assertThatNoException().isThrownBy(
                () -> serviceRequestService.withdrawRequest(1L, 100L));

        verify(serviceRequestRepository).delete(submittedRequest);
    }

    @Test
    @DisplayName("Should throw InvalidOperationException when withdrawing an ASSIGNED request")
    void shouldThrowWhenWithdrawingAssignedRequest() {
        when(serviceRequestRepository.findById(2L)).thenReturn(Optional.of(assignedRequest));

        assertThatThrownBy(() -> serviceRequestService.withdrawRequest(2L, 100L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Only SUBMITTED requests can be withdrawn");
    }

    // ── getRequestUpdates ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return request update history")
    void shouldReturnRequestUpdates() {
        RequestUpdate update = RequestUpdate.builder()
                .updateId(1L)
                .serviceRequest(assignedRequest)
                .officerUserId(200L)
                .officerName("Officer Bob")
                .notes("Assigned")
                .status(ServiceRequestStatus.ASSIGNED)
                .build();

        when(serviceRequestRepository.findById(2L)).thenReturn(Optional.of(assignedRequest));
        when(requestUpdateRepository.findByServiceRequest_RequestIdOrderByCreatedAtAsc(2L))
                .thenReturn(List.of(update));

        List<RequestUpdateResponse> updates = serviceRequestService.getRequestUpdates(2L);

        assertThat(updates).hasSize(1);
        assertThat(updates.get(0).getOfficerUserId()).isEqualTo(200L);
    }
}
