package com.civicconnect.citizen.dto.response;

import com.civicconnect.citizen.enums.DocType;
import com.civicconnect.citizen.enums.VerificationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CitizenDocumentResponse {

    private Long               documentId;
    private Long               citizenId;
    private DocType            docType;
    private String             fileUri;
    private LocalDateTime      uploadedDate;
    private VerificationStatus verificationStatus;
    private String             remarks;
    private Long               reviewedByUserId;
    private LocalDateTime      reviewedAt;
}
