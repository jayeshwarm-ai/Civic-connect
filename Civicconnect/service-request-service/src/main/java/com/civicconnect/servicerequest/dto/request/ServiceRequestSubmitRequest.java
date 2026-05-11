package com.civicconnect.servicerequest.dto.request;

import com.civicconnect.servicerequest.enums.ServiceRequestType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceRequestSubmitRequest {

    @NotNull(message = "Request type is required")
    private ServiceRequestType type;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Address is required")
    private String address;
}
