package com.civicconnect.servicerequest.dto.response;

import com.civicconnect.servicerequest.enums.ServiceRequestStatus;
import com.civicconnect.servicerequest.enums.ServiceRequestType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ServiceRequestResponse {

    private Long                 requestId;
    private Long                 citizenId;
    private String               citizenName;
    private Long                 assignedOfficerUserId;
    private String               assignedOfficerName;
    private ServiceRequestType   type;
    private String               description;
    private String               state;
    private String               city;
    private String               address;
    private ServiceRequestStatus status;
    private LocalDateTime        createdAt;
    private LocalDateTime        updatedAt;
}
