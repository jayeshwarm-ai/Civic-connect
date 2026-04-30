package com.civicconnect.citizen.feign.dto;

import com.civicconnect.citizen.enums.Role;
import com.civicconnect.citizen.enums.UserStatus;
import lombok.*;

/** Mirrors identity-service UserValidationResponse */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserValidationResponse {
    private Long       userId;
    private String     name;
    private String     email;
    private Role       role;
    private UserStatus status;
    private boolean    exists;
}
