package com.civicconnect.identity.dto.response;

import com.civicconnect.identity.enums.Role;
import com.civicconnect.identity.enums.UserStatus;
import lombok.Builder;
import lombok.Getter;

/**
 * Lightweight response for internal Feign calls.
 * Other microservices call GET /internal/users/{id} and receive this.
 */
@Getter
@Builder
public class UserValidationResponse {

    private Long       userId;
    private String     name;
    private String     email;
    private Role       role;
    private UserStatus status;
    private boolean    exists;
}
