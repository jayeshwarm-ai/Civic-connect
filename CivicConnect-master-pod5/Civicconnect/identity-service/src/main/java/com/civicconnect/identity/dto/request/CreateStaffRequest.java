package com.civicconnect.identity.dto.request;

import com.civicconnect.identity.enums.Role;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateStaffRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
    private String phone;

    @NotNull(message = "Role is required")
    private Role role;  // SERVICE_OFFICER | DEPARTMENT_HEAD | CITY_ADMINISTRATOR | COMPLIANCE_OFFICER
}
