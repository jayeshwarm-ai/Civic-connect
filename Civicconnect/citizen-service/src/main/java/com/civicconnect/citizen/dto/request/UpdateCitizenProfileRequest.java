package com.civicconnect.citizen.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCitizenProfileRequest {

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Contact info is required")
    private String contactInfo;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
    private String phone;
}
