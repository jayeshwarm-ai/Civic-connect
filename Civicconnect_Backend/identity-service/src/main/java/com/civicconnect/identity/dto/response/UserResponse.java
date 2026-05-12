package com.civicconnect.identity.dto.response;

import com.civicconnect.identity.enums.Role;
import com.civicconnect.identity.enums.UserStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserResponse {

    private Long        userId;
    private String      name;
    private String      email;
    private String      phone;
    private Role        role;
    private UserStatus  status;
    private LocalDateTime createdAt;
}
