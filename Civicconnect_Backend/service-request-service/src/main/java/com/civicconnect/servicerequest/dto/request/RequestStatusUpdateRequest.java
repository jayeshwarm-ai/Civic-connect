package com.civicconnect.servicerequest.dto.request;

import com.civicconnect.servicerequest.enums.ServiceRequestStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private ServiceRequestStatus status;   // Only IN_PROGRESS or RESOLVED allowed from officer

    @NotBlank(message = "Notes are required")
    private String notes;
}
