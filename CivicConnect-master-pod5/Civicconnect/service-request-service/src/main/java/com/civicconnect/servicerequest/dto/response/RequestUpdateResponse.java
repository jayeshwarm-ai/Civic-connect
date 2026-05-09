package com.civicconnect.servicerequest.dto.response;

import com.civicconnect.servicerequest.enums.ServiceRequestStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RequestUpdateResponse {

    private Long                 updateId;
    private Long                 requestId;
    private Long                 officerUserId;
    private String               officerName;
    private String               notes;
    private ServiceRequestStatus status;
    private LocalDateTime        createdAt;
}
