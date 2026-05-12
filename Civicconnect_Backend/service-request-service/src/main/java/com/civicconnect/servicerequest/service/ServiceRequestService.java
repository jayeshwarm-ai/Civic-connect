package com.civicconnect.servicerequest.service;

import com.civicconnect.servicerequest.dto.request.AssignOfficerRequest;
import com.civicconnect.servicerequest.dto.request.RequestStatusUpdateRequest;
import com.civicconnect.servicerequest.dto.request.ServiceRequestSubmitRequest;
import com.civicconnect.servicerequest.dto.response.RequestUpdateResponse;
import com.civicconnect.servicerequest.dto.response.ServiceRequestResponse;
import com.civicconnect.servicerequest.enums.ServiceRequestStatus;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

/**
 * Service interface for ServiceRequest operations.
 */
public interface ServiceRequestService {

    // ── Submit Request ────────────────────────────────────────────────────────
    ServiceRequestResponse submitRequest(Long citizenUserId, ServiceRequestSubmitRequest request);

    // ── Assign Officer ────────────────────────────────────────────────────────
    ServiceRequestResponse assignOfficer(Long requestId, AssignOfficerRequest request, Long adminUserId);

    // ── Update Status ─────────────────────────────────────────────────────────
    ServiceRequestResponse updateRequestStatus(Long requestId, RequestStatusUpdateRequest request, Long officerUserId);

    // ── Close Request ─────────────────────────────────────────────────────────
    ServiceRequestResponse closeRequest(Long requestId, Long citizenUserId);

    // ── Withdraw Request ──────────────────────────────────────────────────────
    void withdrawRequest(Long requestId, Long citizenUserId);

    // ── Update Request (edit SUBMITTED request) ─────────────────────────────
    ServiceRequestResponse updateRequest(Long requestId, Long citizenUserId, ServiceRequestSubmitRequest request);

    // ── Get Requests ──────────────────────────────────────────────────────────
    ServiceRequestResponse getRequestById(Long requestId);
    ServiceRequestResponse getRequestById(Long requestId, Long loggedInUserId, Collection<? extends GrantedAuthority> authorities);
    List<ServiceRequestResponse> getMyRequests(Long citizenUserId);
    List<ServiceRequestResponse> getRequestsByCitizenId(Long citizenId);
    List<ServiceRequestResponse> getRequestsByCitizenId(Long citizenId, Long loggedInUserId, Collection<? extends GrantedAuthority> authorities);
    List<ServiceRequestResponse> getRequestsByOfficerId(Long officerUserId);
    List<ServiceRequestResponse> getRequestsByStatus(ServiceRequestStatus status, Long loggedInUserId, Collection<? extends GrantedAuthority> authorities);

    // ── Get Update History ────────────────────────────────────────────────────
    List<RequestUpdateResponse> getRequestUpdates(Long requestId);
    List<RequestUpdateResponse> getRequestUpdates(Long requestId, Long loggedInUserId, Collection<? extends GrantedAuthority> authorities);
}

