package com.civicconnect.compliance.service;

import com.civicconnect.compliance.dto.request.CreateAuditRecordRequest;
import com.civicconnect.compliance.dto.request.CreateComplianceRecordRequest;
import com.civicconnect.compliance.dto.request.UpdateAuditRecordRequest;
import com.civicconnect.compliance.dto.response.AuditRecordResponse;
import com.civicconnect.compliance.dto.response.ComplianceRecordResponse;
import com.civicconnect.compliance.enums.AuditStatus;
import com.civicconnect.compliance.enums.ComplianceResult;
import com.civicconnect.compliance.enums.ComplianceType;

import java.util.List;

/**
 * Service interface for Compliance operations.
 */
public interface ComplianceService {
    // Compliance Records
    ComplianceRecordResponse createComplianceRecord(CreateComplianceRecordRequest request, Long officerUserId);
    ComplianceRecordResponse getComplianceRecordById(Long complianceId);
    List<ComplianceRecordResponse> getComplianceRecordsByEntity(ComplianceType type, Long entityId);
    List<ComplianceRecordResponse> getComplianceRecordsByResult(ComplianceResult result);

    // Audit Records
    AuditRecordResponse createAuditRecord(CreateAuditRecordRequest request, Long officerUserId);
    AuditRecordResponse updateAuditRecord(Long auditId, UpdateAuditRecordRequest request, Long officerUserId);
    AuditRecordResponse getAuditRecordById(Long auditId);
    List<AuditRecordResponse> getAuditsByOfficerId(Long officerUserId);
    List<AuditRecordResponse> getAuditsByStatus(AuditStatus status);
}
