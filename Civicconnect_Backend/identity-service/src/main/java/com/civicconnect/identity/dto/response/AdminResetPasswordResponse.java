package com.civicconnect.identity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Returned when an admin resets a staff member's password. Contains the
 * plaintext temporary password. This is the ONLY time it is visible —
 * the admin must communicate it to the staff member out-of-band.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminResetPasswordResponse {
    private Long userId;
    private String name;
    private String email;
    private String temporaryPassword;
    private String message;
}
