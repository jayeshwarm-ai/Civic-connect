package com.civicconnect.identity.service;

import com.civicconnect.identity.dto.request.CreateAuditRecordRequest;
import com.civicconnect.identity.dto.request.UpdateAuditRecordRequest;
import com.civicconnect.identity.dto.response.AuditRecordResponse;
import com.civicconnect.identity.enums.AuditStatus;

import java.util.List;

/**
 * Service interface for AuditRecord operations.
 */
public interface AuditRecordService {
    AuditRecordResponse createAuditRecord(CreateAuditRecordRequest request, Long officerId);
    AuditRecordResponse updateAuditRecord(Long auditId, UpdateAuditRecordRequest request, Long officerId);
    AuditRecordResponse getById(Long auditId);
    List<AuditRecordResponse> getAll();
    List<AuditRecordResponse> getByOfficer(Long officerId);
    List<AuditRecordResponse> getByStatus(AuditStatus status);
}
