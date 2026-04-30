package com.civicconnect.servicerequest.dto.request;

import com.civicconnect.servicerequest.enums.ServiceRequestStatus;
import lombok.Getter;
import lombok.Setter;

/** Used by resolution-service via internal Feign to push status changes back */
@Getter
@Setter
public class StatusUpdateInternalRequest {
    private ServiceRequestStatus status;
}
