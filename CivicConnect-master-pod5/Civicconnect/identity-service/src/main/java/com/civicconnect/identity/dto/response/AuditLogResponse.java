package com.civicconnect.identity.dto.response;

import com.civicconnect.identity.enums.AuditAction;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AuditLogResponse {

    private Long        auditId;
    private Long        performedBy;
    private AuditAction action;
    private String      resource;
    private String      resourceId;
    private String      detail;
    private LocalDateTime timestamp;
}
