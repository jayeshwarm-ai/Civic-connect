package com.civicconnect.identity.dto.response;

import com.civicconnect.identity.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class LoginResponse {

    private Long   userId;
    private String name;
    private String email;
    private Role   role;
    private String token;
    private String tokenType;

    /**
     * When true, the user must change their password before they can use any
     * other API. Set after an admin-triggered staff password reset.
     */
    private boolean mustChangePassword;
}
