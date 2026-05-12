package com.civicconnect.servicerequest.service.impl;

import com.civicconnect.servicerequest.dto.request.AssignOfficerRequest;
import com.civicconnect.servicerequest.dto.request.RequestStatusUpdateRequest;
import com.civicconnect.servicerequest.dto.request.ServiceRequestSubmitRequest;
import com.civicconnect.servicerequest.dto.response.RequestUpdateResponse;
import com.civicconnect.servicerequest.dto.response.ServiceRequestResponse;
import com.civicconnect.servicerequest.entity.RequestUpdate;
import com.civicconnect.servicerequest.entity.ServiceRequest;
import com.civicconnect.servicerequest.enums.NotificationCategory;
import com.civicconnect.servicerequest.enums.ServiceRequestStatus;
import com.civicconnect.servicerequest.exception.InvalidOperationException;
import com.civicconnect.servicerequest.exception.ResourceNotFoundException;
import com.civicconnect.servicerequest.feign.CitizenFeignClient;
import com.civicconnect.servicerequest.feign.IdentityFeignClient;
import com.civicconnect.servicerequest.feign.NotificationFeignClient;
import com.civicconnect.servicerequest.feign.dto.AuditLogRequest;
import com.civicconnect.servicerequest.feign.dto.CitizenValidationResponse;
import com.civicconnect.servicerequest.feign.dto.SendNotificationRequest;
import com.civicconnect.servicerequest.feign.dto.UserValidationResponse;
import com.civicconnect.servicerequest.repository.RequestUpdateRepository;
import com.civicconnect.servicerequest.repository.ServiceRequestRepository;
import com.civicconnect.servicerequest.service.ServiceRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceRequestServiceImpl implements ServiceRequestService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final RequestUpdateRepository  requestUpdateRepository;
    private final CitizenFeignClient       citizenFeignClient;
    private final IdentityFeignClient      identityFeignClient;
    private final NotificationFeignClient  notificationFeignClient;

    @Override
    @Transactional
    public ServiceRequestResponse submitRequest(Long citizenUserId, ServiceRequestSubmitRequest request) {
        CitizenValidationResponse citizen = citizenFeignClient.getCitizenByUserId(citizenUserId);
        if (!citizen.isExists()) {
            throw new ResourceNotFoundException("Citizen profile not found for userId: " + citizenUserId);
        }
        if (!"ACTIVE".equals(citizen.getAccountStatus())) {
            throw new InvalidOperationException(
                    "Only citizens with ACTIVE accounts can submit service requests. Current status: " + citizen.getAccountStatus());
        }

        ServiceRequest serviceRequest = ServiceRequest.builder()
                .citizenId(citizen.getCitizenId())
                .citizenName(citizen.getName())
                .citizenUserId(citizenUserId)
                .type(request.getType())
                .description(request.getDescription())
                .state(request.getState())
                .city(request.getCity())
                .address(request.getAddress())
                .build();

        serviceRequest = serviceRequestRepository.save(serviceRequest);

        String locationLog = request.getCity() + ", " + request.getState();

        writeAuditLog(citizenUserId, "SERVICE_REQUEST_CREATED",
                "SERVICE_REQUEST", String.valueOf(serviceRequest.getRequestId()),
                "Type: " + request.getType() + " | Location: " + locationLog);

        sendNotification(citizenUserId, serviceRequest.getRequestId(),
                "Your service request #" + serviceRequest.getRequestId() + " has been submitted successfully.",
                NotificationCategory.REQUEST);

        notifyAllAdmins(serviceRequest.getRequestId(),
                "📋 New service request #" + serviceRequest.getRequestId()
                + " submitted by " + citizen.getName()
                + " | Type: " + request.getType()
                + " | Location: " + locationLog);

        return mapToResponse(serviceRequest);
    }

    @Override
    @Transactional
    public ServiceRequestResponse assignOfficer(Long requestId, AssignOfficerRequest request, Long adminUserId) {
        ServiceRequest serviceRequest = findById(requestId);

        if (serviceRequest.getStatus() != ServiceRequestStatus.SUBMITTED) {
            throw new InvalidOperationException("Only SUBMITTED requests can be assigned. Current status: " + serviceRequest.getStatus());
        }

        UserValidationResponse officer = identityFeignClient.validateUser(request.getOfficerId());
        if (!officer.isExists()) {
            throw new ResourceNotFoundException("Officer", request.getOfficerId());
        }
        if (!"SERVICE_OFFICER".equals(officer.getRole())) {
            throw new InvalidOperationException("User with ID " + request.getOfficerId() + " is not a SERVICE_OFFICER. Role: " + officer.getRole());
        }

        serviceRequest.setAssignedOfficerUserId(officer.getUserId());
        serviceRequest.setAssignedOfficerName(officer.getName());
        serviceRequest.setStatus(ServiceRequestStatus.ASSIGNED);
        serviceRequestRepository.save(serviceRequest);

        RequestUpdate update = RequestUpdate.builder()
                .serviceRequest(serviceRequest)
                .officerUserId(officer.getUserId())
                .officerName(officer.getName())
                .notes("Request received and assigned to officer: " + officer.getName())
                .status(ServiceRequestStatus.ASSIGNED)
                .build();
        requestUpdateRepository.save(update);

        writeAuditLog(adminUserId, "SERVICE_REQUEST_ASSIGNED",
                "SERVICE_REQUEST", String.valueOf(requestId),
                "Assigned to officerId: " + officer.getUserId());

        sendNotification(officer.getUserId(), requestId,
                "You have been assigned to service request #" + requestId + ".",
                NotificationCategory.REQUEST);

        notifyAllAdmins(requestId,
                "👮 Service request #" + requestId
                + " has been ASSIGNED to officer: " + officer.getName()
                + " (ID: #" + officer.getUserId() + ")");

        return mapToResponse(serviceRequest);
    }

    @Override
    @Transactional
    public ServiceRequestResponse updateRequestStatus(Long requestId, RequestStatusUpdateRequest request, Long officerUserId) {
        ServiceRequest serviceRequest = findById(requestId);

        if (serviceRequest.getAssignedOfficerUserId() == null ||
                !serviceRequest.getAssignedOfficerUserId().equals(officerUserId)) {
            throw new InvalidOperationException("You are not the assigned officer for this request.");
        }

        if (request.getStatus() != ServiceRequestStatus.IN_PROGRESS &&
                request.getStatus() != ServiceRequestStatus.RESOLVED) {
            throw new InvalidOperationException("Officer can only update status to IN_PROGRESS or RESOLVED.");
        }

        validateStatusTransition(serviceRequest.getStatus(), request.getStatus());

        serviceRequest.setStatus(request.getStatus());
        serviceRequestRepository.save(serviceRequest);

        UserValidationResponse officer = identityFeignClient.validateUser(officerUserId);
        String officerName = officer.isExists() ? officer.getName() : "Officer#" + officerUserId;

        RequestUpdate update = RequestUpdate.builder()
                .serviceRequest(serviceRequest)
                .officerUserId(officerUserId)
                .officerName(officerName)
                .notes(request.getNotes())
                .status(request.getStatus())
                .build();
        requestUpdateRepository.save(update);

        writeAuditLog(officerUserId, "SERVICE_REQUEST_STATUS_UPDATED",
                "SERVICE_REQUEST", String.valueOf(requestId),
                "Status → " + request.getStatus() + " | Notes: " + request.getNotes());

        sendNotification(serviceRequest.getCitizenUserId(), requestId,
                "Your service request #" + requestId + " status has been updated to: " + request.getStatus(),
                NotificationCategory.REQUEST);

        notifyAllAdmins(requestId,
                "🔄 Service request #" + requestId
                + " status updated to " + request.getStatus()
                + " by officer: " + officerName);

        return mapToResponse(serviceRequest);
    }

    @Override
    @Transactional
    public ServiceRequestResponse closeRequest(Long requestId, Long citizenUserId) {
        ServiceRequest serviceRequest = findById(requestId);

        if (!serviceRequest.getCitizenUserId().equals(citizenUserId)) {
            throw new InvalidOperationException("You are not authorized to close this request.");
        }
        if (serviceRequest.getStatus() != ServiceRequestStatus.RESOLVED) {
            throw new InvalidOperationException("Request can only be closed when RESOLVED. Current status: " + serviceRequest.getStatus());
        }

        serviceRequest.setStatus(ServiceRequestStatus.CLOSED);
        serviceRequestRepository.save(serviceRequest);

        sendNotification(citizenUserId, requestId,
                "Your service request #" + requestId + " has been CLOSED. Thank you for using CivicConnect.",
                NotificationCategory.REQUEST);

        notifyAllAdmins(requestId,
                "✅ Service request #" + requestId
                + " has been CLOSED by citizen: " + serviceRequest.getCitizenName());

        // Notify compliance officers about closed request pending compliance review
        notifyByRole("COMPLIANCE_OFFICER", requestId,
                "🛡️ Service request #" + requestId
                + " has been CLOSED by citizen: " + serviceRequest.getCitizenName()
                + ". Compliance review pending.");

        writeAuditLog(citizenUserId, "SERVICE_REQUEST_CLOSED",
                "SERVICE_REQUEST", String.valueOf(requestId), "Request closed by citizen");

        return mapToResponse(serviceRequest);
    }

    @Override
    @Transactional
    public void withdrawRequest(Long requestId, Long citizenUserId) {
        ServiceRequest serviceRequest = findById(requestId);

        if (!serviceRequest.getCitizenUserId().equals(citizenUserId)) {
            throw new InvalidOperationException("You are not authorized to withdraw this request.");
        }
        if (serviceRequest.getStatus() != ServiceRequestStatus.SUBMITTED) {
            throw new InvalidOperationException("Only SUBMITTED requests can be withdrawn. Current status: " + serviceRequest.getStatus());
        }

        serviceRequestRepository.delete(serviceRequest);

        notifyAllAdmins(requestId,
                "🗑️ Service request #" + requestId
                + " has been WITHDRAWN by citizen: " + serviceRequest.getCitizenName());

        writeAuditLog(citizenUserId, "SERVICE_REQUEST_WITHDRAWN",
                "SERVICE_REQUEST", String.valueOf(requestId), "Request withdrawn by citizen");
    }

    @Override
    @Transactional
    public ServiceRequestResponse updateRequest(Long requestId, Long citizenUserId, ServiceRequestSubmitRequest request) {
        ServiceRequest serviceRequest = findById(requestId);

        if (!serviceRequest.getCitizenUserId().equals(citizenUserId)) {
            throw new InvalidOperationException("You can only edit your own requests.");
        }
        if (serviceRequest.getStatus() != ServiceRequestStatus.SUBMITTED) {
            throw new InvalidOperationException("Only SUBMITTED requests can be edited.");
        }

        serviceRequest.setType(request.getType());
        serviceRequest.setDescription(request.getDescription());
        serviceRequest.setState(request.getState());
        serviceRequest.setCity(request.getCity());
        serviceRequest.setAddress(request.getAddress());

        ServiceRequest saved = serviceRequestRepository.save(serviceRequest);
        log.info("Service request #{} updated by citizen userId={}", requestId, citizenUserId);

        writeAuditLog(citizenUserId, "SERVICE_REQUEST_UPDATED",
                "SERVICE_REQUEST", String.valueOf(requestId), "Request details updated by citizen");

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceRequestResponse getRequestById(Long requestId) {
        return mapToResponse(findById(requestId));
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceRequestResponse getRequestById(Long requestId, Long loggedInUserId, Collection<? extends GrantedAuthority> authorities) {
        ServiceRequest serviceRequest = findById(requestId);

        boolean isOfficer = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_SERVICE_OFFICER") ||
                                  role.equals("ROLE_DEPARTMENT_HEAD") ||
                                  role.equals("ROLE_CITY_ADMINISTRATOR") ||
                                  role.equals("ROLE_COMPLIANCE_OFFICER"));

        if (!isOfficer && !serviceRequest.getCitizenUserId().equals(loggedInUserId)) {
            throw new InvalidOperationException("This request is not yours");
        }

        return mapToResponse(serviceRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceRequestResponse> getMyRequests(Long citizenUserId) {
        CitizenValidationResponse citizen = citizenFeignClient.getCitizenByUserId(citizenUserId);
        if (!citizen.isExists()) {
            throw new ResourceNotFoundException("Citizen profile not found for userId: " + citizenUserId);
        }
        return serviceRequestRepository.findByCitizenIdOrderByCreatedAtDesc(citizen.getCitizenId())
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceRequestResponse> getRequestsByCitizenId(Long citizenId) {
        CitizenValidationResponse citizen = citizenFeignClient.validateCitizen(citizenId);
        if (!citizen.isExists()) {
            throw new ResourceNotFoundException("Citizen", citizenId);
        }
        return serviceRequestRepository.findByCitizenIdOrderByCreatedAtDesc(citizenId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceRequestResponse> getRequestsByCitizenId(Long citizenId, Long loggedInUserId,
                                                                Collection<? extends GrantedAuthority> authorities) {
        CitizenValidationResponse citizen = citizenFeignClient.validateCitizen(citizenId);
        if (!citizen.isExists()) {
            throw new ResourceNotFoundException("Citizen", citizenId);
        }

        boolean isOfficer = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_SERVICE_OFFICER") ||
                                  role.equals("ROLE_DEPARTMENT_HEAD") ||
                                  role.equals("ROLE_CITY_ADMINISTRATOR"));

        if (!isOfficer) {
            CitizenValidationResponse loggedInCitizen = citizenFeignClient.getCitizenByUserId(loggedInUserId);
            if (!loggedInCitizen.isExists()) {
                throw new ResourceNotFoundException("Citizen profile not found for userId: " + loggedInUserId);
            }
            if (!loggedInCitizen.getCitizenId().equals(citizenId)) {
                throw new InvalidOperationException("You are not authorized to view another citizen's requests.");
            }
        }

        return serviceRequestRepository.findByCitizenIdOrderByCreatedAtDesc(citizenId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceRequestResponse> getRequestsByOfficerId(Long officerUserId) {
        UserValidationResponse officer = identityFeignClient.validateUser(officerUserId);
        if (!officer.isExists()) {
            throw new ResourceNotFoundException("Officer", officerUserId);
        }
        return serviceRequestRepository.findByAssignedOfficerUserIdOrderByCreatedAtDesc(officerUserId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceRequestResponse> getRequestsByStatus(ServiceRequestStatus status, Long loggedInUserId,
                                                             Collection<? extends GrantedAuthority> authorities) {
        boolean isAdmin = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_DEPARTMENT_HEAD")
                        || role.equals("ROLE_CITY_ADMINISTRATOR")
                        || role.equals("ROLE_COMPLIANCE_OFFICER"));

        if (isAdmin) {
            return serviceRequestRepository.findByStatusOrderByCreatedAtAsc(status)
                    .stream().map(this::mapToResponse).collect(Collectors.toList());
        } else {
            return serviceRequestRepository.findByStatusAndAssignedOfficerUserIdOrderByCreatedAtAsc(status, loggedInUserId)
                    .stream().map(this::mapToResponse).collect(Collectors.toList());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestUpdateResponse> getRequestUpdates(Long requestId) {
        findById(requestId);
        return requestUpdateRepository.findByServiceRequest_RequestIdOrderByCreatedAtAsc(requestId)
                .stream().map(this::mapToUpdateResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestUpdateResponse> getRequestUpdates(Long requestId, Long loggedInUserId, Collection<? extends GrantedAuthority> authorities) {
        ServiceRequest serviceRequest = findById(requestId);

        boolean isOfficer = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_SERVICE_OFFICER") ||
                                  role.equals("ROLE_DEPARTMENT_HEAD") ||
                                  role.equals("ROLE_CITY_ADMINISTRATOR") ||
                                  role.equals("ROLE_COMPLIANCE_OFFICER"));

        if (!isOfficer && !serviceRequest.getCitizenUserId().equals(loggedInUserId)) {
            throw new InvalidOperationException("This request is not yours");
        }

        return requestUpdateRepository.findByServiceRequest_RequestIdOrderByCreatedAtAsc(requestId)
                .stream().map(this::mapToUpdateResponse).collect(Collectors.toList());
    }

    // ── PRIVATE HELPERS ───────────────────────────────────────────────────────

    private ServiceRequest findById(Long requestId) {
        return serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceRequest", requestId));
    }

    private void validateStatusTransition(ServiceRequestStatus current, ServiceRequestStatus next) {
        boolean valid = switch (current) {
            case ASSIGNED    -> next == ServiceRequestStatus.IN_PROGRESS;
            case IN_PROGRESS -> next == ServiceRequestStatus.RESOLVED;
            default          -> false;
        };
        if (!valid) {
            throw new InvalidOperationException("Invalid status transition: " + current + " → " + next);
        }
    }

    private void writeAuditLog(Long performedBy, String action, String resource, String resourceId, String detail) {
        try {
            identityFeignClient.writeAuditLog(AuditLogRequest.builder()
                    .performedBy(performedBy).action(action).resource(resource).resourceId(resourceId).detail(detail)
                    .build());
        } catch (Exception e) {
            log.warn("Failed to write audit log: action={}, resource={}: {}", action, resource, e.getMessage());
        }
    }

    private void sendNotification(Long userId, Long requestId, String message, NotificationCategory category) {
        try {
            notificationFeignClient.sendNotification(SendNotificationRequest.builder()
                    .userId(userId).requestId(requestId).message(message).category(category.name())
                    .build());
        } catch (Exception e) {
            log.warn("Failed to send notification: userId={}: {}", userId, e.getMessage());
        }
    }

    private void notifyAllAdmins(Long requestId, String message) {
        notifyByRole("CITY_ADMINISTRATOR", requestId, message);
    }

    private void notifyByRole(String role, Long requestId, String message) {
        try {
            List<Long> userIds = identityFeignClient.getUserIdsByRole(role);
            for (Long uid : userIds) {
                sendNotification(uid, requestId, message, NotificationCategory.REQUEST);
            }
        } catch (Exception e) {
            log.warn("Failed to notify role={}: {}", role, e.getMessage());
        }
    }

    // ── MAPPERS ───────────────────────────────────────────────────────────────

    private ServiceRequestResponse mapToResponse(ServiceRequest sr) {
        return ServiceRequestResponse.builder()
                .requestId(sr.getRequestId())
                .citizenId(sr.getCitizenId())
                .citizenName(sr.getCitizenName())
                .assignedOfficerUserId(sr.getAssignedOfficerUserId())
                .assignedOfficerName(sr.getAssignedOfficerName())
                .type(sr.getType())
                .description(sr.getDescription())
                .state(sr.getState())
                .city(sr.getCity())
                .address(sr.getAddress())
                .status(sr.getStatus())
                .createdAt(sr.getCreatedAt())
                .updatedAt(sr.getUpdatedAt())
                .build();
    }

    private RequestUpdateResponse mapToUpdateResponse(RequestUpdate ru) {
        return RequestUpdateResponse.builder()
                .updateId(ru.getUpdateId())
                .requestId(ru.getServiceRequest().getRequestId())
                .officerUserId(ru.getOfficerUserId())
                .officerName(ru.getOfficerName())
                .notes(ru.getNotes())
                .status(ru.getStatus())
                .createdAt(ru.getCreatedAt())
                .build();
    }
}

