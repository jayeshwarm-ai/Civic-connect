package com.civicconnect.compliance.service.impl;

import com.civicconnect.compliance.dto.request.CreateAuditRecordRequest;
import com.civicconnect.compliance.dto.request.CreateComplianceRecordRequest;
import com.civicconnect.compliance.dto.request.UpdateAuditRecordRequest;
import com.civicconnect.compliance.dto.response.AuditRecordResponse;
import com.civicconnect.compliance.dto.response.ComplianceRecordResponse;
import com.civicconnect.compliance.entity.AuditRecord;
import com.civicconnect.compliance.entity.ComplianceRecord;
import com.civicconnect.compliance.enums.AuditStatus;
import com.civicconnect.compliance.enums.ComplianceResult;
import com.civicconnect.compliance.enums.ComplianceType;
import com.civicconnect.compliance.enums.NotificationCategory;
import com.civicconnect.compliance.exception.InvalidOperationException;
import com.civicconnect.compliance.exception.ResourceNotFoundException;
import com.civicconnect.compliance.feign.*;
import com.civicconnect.compliance.feign.dto.*;
import com.civicconnect.compliance.repository.AuditRecordRepository;
import com.civicconnect.compliance.repository.ComplianceRecordRepository;
import com.civicconnect.compliance.service.ComplianceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComplianceServiceImpl implements ComplianceService {

    private final ComplianceRecordRepository complianceRecordRepository;
    private final AuditRecordRepository      auditRecordRepository;
    private final IdentityFeignClient        identityFeignClient;
    private final ServiceRequestFeignClient  serviceRequestFeignClient;
    private final ResolutionFeignClient      resolutionFeignClient;
    private final NotificationFeignClient    notificationFeignClient;

    @Override
    @Transactional
    public ComplianceRecordResponse createComplianceRecord(CreateComplianceRecordRequest request, Long officerUserId) {
        UserValidationResponse officer = identityFeignClient.validateUser(officerUserId);
        if (!officer.isExists()) throw new ResourceNotFoundException("User", officerUserId);
        // Both COMPLIANCE_OFFICER and CITY_ADMINISTRATOR can file compliance records.
        // City admins act as auditors and need parity with compliance officers for oversight.
        if (!"COMPLIANCE_OFFICER".equals(officer.getRole())
                && !"CITY_ADMINISTRATOR".equals(officer.getRole()))
            throw new InvalidOperationException("Only COMPLIANCE_OFFICER or CITY_ADMINISTRATOR can create compliance records.");

        if (request.getType() == ComplianceType.REQUEST) {
            ServiceRequestValidationResponse sr = serviceRequestFeignClient.getRequest(request.getEntityId());
            if (!sr.isExists()) throw new ResourceNotFoundException("ServiceRequest not found with id: " + request.getEntityId());
            // A compliance record can only be filed once the citizen has confirmed closure of the request.
            // Blocks records being created on SUBMITTED / ASSIGNED / IN_PROGRESS / RESOLVED requests.
            if (!"CLOSED".equalsIgnoreCase(sr.getStatus())) {
                throw new InvalidOperationException(
                        "Compliance records can only be created for CLOSED service requests. "
                        + "Request #" + request.getEntityId() + " is currently in '" + sr.getStatus() + "' state."
                );
            }
        } else if (request.getType() == ComplianceType.RESOLUTION) {
            ResolutionValidationResponse res = resolutionFeignClient.getResolution(request.getEntityId());
            if (!res.isExists()) throw new ResourceNotFoundException("Resolution not found with id: " + request.getEntityId());
            // A compliance record can only be filed once the resolution is fully completed.
            // Blocks records being created on IN_PROGRESS resolutions.
            if (!"COMPLETED".equalsIgnoreCase(res.getStatus())) {
                throw new InvalidOperationException(
                        "Compliance records can only be created for COMPLETED resolutions. "
                        + "Resolution #" + request.getEntityId() + " is currently in '" + res.getStatus() + "' state."
                );
            }
        }

        ComplianceRecord record = ComplianceRecord.builder()
                .type(request.getType()).entityId(request.getEntityId())
                .createdByUserId(officerUserId).createdByName(officer.getName())
                .result(request.getResult()).notes(request.getNotes())
                .build();
        record = complianceRecordRepository.save(record);

        writeAuditLog(officerUserId, "COMPLIANCE_RECORD_CREATED", "COMPLIANCE_RECORD",
                String.valueOf(record.getComplianceId()),
                "Type: " + request.getType() + " | EntityId: " + request.getEntityId() + " | Result: " + request.getResult());

        if (request.getResult() == ComplianceResult.FAIL) {
            sendNotification(officerUserId, null,
                    "Compliance FAIL recorded for " + request.getType() + " ID: " + request.getEntityId(),
                    NotificationCategory.COMPLIANCE);
        }
        return mapToComplianceResponse(record);
    }

    @Override
    @Transactional(readOnly = true)
    public ComplianceRecordResponse getComplianceRecordById(Long complianceId) {
        return mapToComplianceResponse(complianceRecordRepository.findById(complianceId)
                .orElseThrow(() -> new ResourceNotFoundException("ComplianceRecord", complianceId)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComplianceRecordResponse> getComplianceRecordsByEntity(ComplianceType type, Long entityId) {
        return complianceRecordRepository.findByTypeAndEntityIdOrderByCreatedAtDesc(type, entityId)
                .stream().map(this::mapToComplianceResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComplianceRecordResponse> getComplianceRecordsByResult(ComplianceResult result) {
        return complianceRecordRepository.findByResultOrderByCreatedAtDesc(result)
                .stream().map(this::mapToComplianceResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AuditRecordResponse createAuditRecord(CreateAuditRecordRequest request, Long officerUserId) {
        UserValidationResponse officer = identityFeignClient.validateUser(officerUserId);
        if (!officer.isExists()) throw new ResourceNotFoundException("User", officerUserId);
        // Both COMPLIANCE_OFFICER and CITY_ADMINISTRATOR can create audits.
        if (!"COMPLIANCE_OFFICER".equals(officer.getRole())
                && !"CITY_ADMINISTRATOR".equals(officer.getRole()))
            throw new InvalidOperationException("Only COMPLIANCE_OFFICER or CITY_ADMINISTRATOR can create audit records.");

        AuditRecord auditRecord = AuditRecord.builder()
                .officerUserId(officerUserId).officerName(officer.getName()).scope(request.getScope())
                .build();
        auditRecord = auditRecordRepository.save(auditRecord);

        writeAuditLog(officerUserId, "AUDIT_CREATED", "AUDIT_RECORD",
                String.valueOf(auditRecord.getAuditId()), "Audit created with scope: " + request.getScope());
        return mapToAuditResponse(auditRecord);
    }

    @Override
    @Transactional
    public AuditRecordResponse updateAuditRecord(Long auditId, UpdateAuditRecordRequest request, Long officerUserId) {
        AuditRecord auditRecord = auditRecordRepository.findById(auditId)
                .orElseThrow(() -> new ResourceNotFoundException("AuditRecord", auditId));

        // Allow the original officer OR a CITY_ADMINISTRATOR to update the audit.
        // Admins need oversight powers — they should be able to close/edit any audit.
        UserValidationResponse caller = identityFeignClient.validateUser(officerUserId);
        boolean isOriginalOfficer = auditRecord.getOfficerUserId().equals(officerUserId);
        boolean isCityAdmin       = caller.isExists() && "CITY_ADMINISTRATOR".equals(caller.getRole());
        if (!isOriginalOfficer && !isCityAdmin)
            throw new InvalidOperationException("Only the officer who created this audit, or a City Administrator, can update it.");
        if (auditRecord.getStatus() == AuditStatus.CLOSED)
            throw new InvalidOperationException("Cannot update a CLOSED audit record.");

        validateAuditTransition(auditRecord.getStatus(), request.getStatus());
        auditRecord.setStatus(request.getStatus());

        if (request.getFindings() != null && !request.getFindings().isBlank()) {
            auditRecord.setFindings(request.getFindings());
        }
        auditRecordRepository.save(auditRecord);

        writeAuditLog(officerUserId, "AUDIT_STATUS_UPDATED", "AUDIT_RECORD",
                String.valueOf(auditId), "Audit status updated to: " + request.getStatus());
        return mapToAuditResponse(auditRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public AuditRecordResponse getAuditRecordById(Long auditId) {
        return mapToAuditResponse(auditRecordRepository.findById(auditId)
                .orElseThrow(() -> new ResourceNotFoundException("AuditRecord", auditId)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditRecordResponse> getAuditsByOfficerId(Long officerUserId) {
        UserValidationResponse officer = identityFeignClient.validateUser(officerUserId);
        if (!officer.isExists()) throw new ResourceNotFoundException("User", officerUserId);
        return auditRecordRepository.findByOfficerUserIdOrderByCreatedAtDesc(officerUserId)
                .stream().map(this::mapToAuditResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditRecordResponse> getAuditsByStatus(AuditStatus status) {
        return auditRecordRepository.findByStatusOrderByCreatedAtDesc(status)
                .stream().map(this::mapToAuditResponse).collect(Collectors.toList());
    }

    private void validateAuditTransition(AuditStatus current, AuditStatus next) {
        boolean valid = switch (current) {
            case OPEN -> next == AuditStatus.IN_REVIEW;
            case IN_REVIEW -> next == AuditStatus.CLOSED;
            case CLOSED -> false;
        };
        if (!valid) throw new InvalidOperationException("Invalid audit status transition: " + current + " → " + next);
    }

    private void writeAuditLog(Long performedBy, String action, String resource, String resourceId, String detail) {
        try {
            identityFeignClient.writeAuditLog(AuditLogRequest.builder()
                    .performedBy(performedBy).action(action).resource(resource).resourceId(resourceId).detail(detail).build());
        } catch (Exception e) { log.warn("Failed to write audit log: action={}: {}", action, e.getMessage()); }
    }

    private void sendNotification(Long userId, Long requestId, String message, NotificationCategory category) {
        try {
            notificationFeignClient.sendNotification(SendNotificationRequest.builder()
                    .userId(userId).requestId(requestId).message(message).category(category.name()).build());
        } catch (Exception e) { log.warn("Failed to send notification to userId={}: {}", userId, e.getMessage()); }
    }

    private ComplianceRecordResponse mapToComplianceResponse(ComplianceRecord r) {
        return ComplianceRecordResponse.builder()
                .complianceId(r.getComplianceId()).type(r.getType()).entityId(r.getEntityId())
                .createdByUserId(r.getCreatedByUserId()).createdByName(r.getCreatedByName())
                .result(r.getResult()).notes(r.getNotes()).createdAt(r.getCreatedAt()).build();
    }

    private AuditRecordResponse mapToAuditResponse(AuditRecord a) {
        return AuditRecordResponse.builder()
                .auditId(a.getAuditId()).officerUserId(a.getOfficerUserId()).officerName(a.getOfficerName())
                .scope(a.getScope()).findings(a.getFindings()).status(a.getStatus())
                .createdAt(a.getCreatedAt()).updatedAt(a.getUpdatedAt()).build();
    }
}

