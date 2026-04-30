package com.civicconnect.reporting.feign.dto;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserValidationResponse {
    private Long userId; private String name; private String email;
    private String role; private String status; private boolean exists;
}
