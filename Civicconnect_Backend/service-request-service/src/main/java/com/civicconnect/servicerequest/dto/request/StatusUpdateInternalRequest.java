package com.civicconnect.servicerequest.dto.request;

import com.civicconnect.servicerequest.enums.ServiceRequestStatus;
import lombok.Getter;
import lombok.Setter;

/**
 * Used by resolution-service via internal Feign to push status changes back.
 * The actor + notes fields are needed so we can write a meaningful
 * RequestUpdate audit row alongside the status change.
 */
@Getter
@Setter
public class StatusUpdateInternalRequest {
    private ServiceRequestStatus status;

    /** Actor making the change — required so request_updates can record who/what/why */
    private Long   officerUserId;
    private String officerName;   // already includes role suffix e.g. "John (Service Officer)"
    private String notes;
}
