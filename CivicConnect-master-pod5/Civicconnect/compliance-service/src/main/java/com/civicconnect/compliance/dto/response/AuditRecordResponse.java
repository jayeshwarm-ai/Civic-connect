package com.civicconnect.compliance.dto.response;

import com.civicconnect.compliance.enums.AuditStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AuditRecordResponse {

    private Long          auditId;
    private Long          officerUserId;
    private String        officerName;
    private String        scope;
    private String        findings;
    private AuditStatus   status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
