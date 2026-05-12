package com.civicconnect.servicerequest.dto.response;

import com.civicconnect.servicerequest.enums.ServiceRequestStatus;
import com.civicconnect.servicerequest.enums.ServiceRequestType;
import lombok.Builder;
import lombok.Getter;

/**
 * Lightweight response for internal Feign calls.
 * resolution-service and feedback-service call
 * GET /internal/service-requests/{id} and receive this.
 */
@Getter
@Builder
public class ServiceRequestValidationResponse {

    private Long                 requestId;
    private Long                 citizenId;
    private Long                 citizenUserId;
    private Long                 assignedOfficerUserId;
    private ServiceRequestType   type;
    private ServiceRequestStatus status;
    private boolean              exists;
}
