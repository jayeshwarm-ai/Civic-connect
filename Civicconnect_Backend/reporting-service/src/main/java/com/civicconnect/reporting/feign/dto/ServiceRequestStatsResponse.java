package com.civicconnect.reporting.feign.dto;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ServiceRequestStatsResponse {
    private long total, submitted, assigned, inProgress, resolved, closed;
    private long road, water, electricity;
}
