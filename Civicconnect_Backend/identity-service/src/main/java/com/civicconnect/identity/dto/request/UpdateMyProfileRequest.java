package com.civicconnect.identity.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * Request body for a staff user updating their own profile.
 *
 * Allowed fields: email, phone. Name and role are administered centrally and
 * cannot be self-edited. Citizens use a separate citizen-service endpoint
 * (/api/v1/citizens/my-profile) and don't go through this DTO.
 */
@Getter
@Setter
public class UpdateMyProfileRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be exactly 10 digits")
    private String phone;
}
