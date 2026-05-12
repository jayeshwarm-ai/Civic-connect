package com.civicconnect.compliance.feign.dto;

import lombok.*;

/** Mirrors UserValidationResponse from identity-service */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserValidationResponse {
    private Long    userId;
    private String  name;
    private String  email;
    private String  role;    // Role as String e.g. "COMPLIANCE_OFFICER"
    private String  status;  // UserStatus as String
    private boolean exists;
}
