package com.civicconnect.citizen.feign.dto;

import lombok.*;

/** Sent to identity-service to create a new User row during citizen registration */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegistrationRequest {
    private String name;
    private String email;
    private String password;   // raw password — identity-service hashes it
    private String phone;
}
