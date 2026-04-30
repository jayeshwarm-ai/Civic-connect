package com.civicconnect.citizen.feign.dto;

import lombok.*;

/** Sent to identity-service to write an audit trail entry */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogRequest {
    private Long   performedBy;
    private String action;      // AuditAction enum name as String
    private String resource;
    private String resourceId;
    private String detail;
}
