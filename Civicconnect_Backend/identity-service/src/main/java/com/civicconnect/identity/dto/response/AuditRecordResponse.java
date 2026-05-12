package com.civicconnect.identity.dto.response;

import com.civicconnect.identity.enums.AuditStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AuditRecordResponse {

    private Long        auditId;
    private Long        officerId;
    private String      officerName;
    private String      scope;
    private String      findings;
    private AuditStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
