package com.civicconnect.citizen.dto.response;

import com.civicconnect.citizen.enums.UserStatus;
import lombok.Builder;
import lombok.Getter;

/**
 * Lightweight response for internal Feign calls.
 * service-request-service and feedback-service call
 * GET /internal/citizens/{id} and receive this.
 */
@Getter
@Builder
public class CitizenValidationResponse {

    private Long       citizenId;
    private Long       userId;
    private String     name;
    private String     email;
    private UserStatus accountStatus;
    private boolean    exists;
}
