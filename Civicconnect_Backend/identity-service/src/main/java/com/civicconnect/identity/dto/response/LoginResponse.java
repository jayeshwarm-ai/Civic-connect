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
}
