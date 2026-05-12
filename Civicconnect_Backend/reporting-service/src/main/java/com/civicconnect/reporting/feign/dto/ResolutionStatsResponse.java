package com.civicconnect.reporting.feign.dto;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ResolutionStatsResponse {
    private long total, inProgress, completed;
}
