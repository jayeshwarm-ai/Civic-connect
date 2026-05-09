package com.civicconnect.resolution.dto.response;

import com.civicconnect.resolution.enums.ResolutionStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ResolutionResponse {

    private Long             resolutionId;
    private Long             requestId;
    private Long             officerUserId;
    private String           officerName;
    private String           actions;
    private ResolutionStatus status;
    private LocalDateTime    createdAt;
    private LocalDateTime    updatedAt;
}
