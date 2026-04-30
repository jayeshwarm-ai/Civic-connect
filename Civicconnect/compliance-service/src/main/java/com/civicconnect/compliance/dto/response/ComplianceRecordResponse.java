package com.civicconnect.compliance.dto.response;

import com.civicconnect.compliance.enums.ComplianceResult;
import com.civicconnect.compliance.enums.ComplianceType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ComplianceRecordResponse {

    private Long            complianceId;
    private ComplianceType  type;
    private Long            entityId;
    private Long            createdByUserId;
    private String          createdByName;
    private ComplianceResult result;
    private String          notes;
    private LocalDateTime   createdAt;
}
