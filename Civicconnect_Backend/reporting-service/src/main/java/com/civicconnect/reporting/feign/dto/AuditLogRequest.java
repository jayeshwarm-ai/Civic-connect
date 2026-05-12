package com.civicconnect.reporting.feign.dto;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLogRequest {
    private Long performedBy; private String action;
    private String resource; private String resourceId; private String detail;
}
