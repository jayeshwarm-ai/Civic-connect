package com.civicconnect.identity.service.impl;

import com.civicconnect.identity.audit.AuditLogService;
import com.civicconnect.identity.dto.request.CreateAuditRecordRequest;
import com.civicconnect.identity.dto.request.UpdateAuditRecordRequest;
import com.civicconnect.identity.dto.response.AuditRecordResponse;
import com.civicconnect.identity.entity.AuditRecord;
import com.civicconnect.identity.entity.User;
import com.civicconnect.identity.enums.AuditAction;
import com.civicconnect.identity.enums.AuditStatus;
import com.civicconnect.identity.exception.InvalidOperationException;
import com.civicconnect.identity.exception.ResourceNotFoundException;
import com.civicconnect.identity.repository.AuditRecordRepository;
import com.civicconnect.identity.repository.UserRepository;
import com.civicconnect.identity.service.AuditRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditRecordServiceImpl implements AuditRecordService {

    private final AuditRecordRepository auditRecordRepository;
    private final UserRepository        userRepository;
    private final AuditLogService       auditLogService;

    @Override
    @Transactional
    public AuditRecordResponse createAuditRecord(CreateAuditRecordRequest request, Long officerId) {
        User officer = userRepository.findById(officerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", officerId));

        AuditRecord record = AuditRecord.builder()
                .officer(officer)
                .scope(request.getScope())
                .build();

        record = auditRecordRepository.save(record);

        auditLogService.log(officerId, AuditAction.AUDIT_CREATED, "AUDIT_RECORD",
                String.valueOf(record.getAuditId()), "Audit created. Scope: " + request.getScope());

        return mapToResponse(record);
    }

    @Override
    @Transactional
    public AuditRecordResponse updateAuditRecord(Long auditId, UpdateAuditRecordRequest request, Long officerId) {
        AuditRecord record = findById(auditId);

        if (!record.getOfficer().getUserId().equals(officerId)) {
            throw new InvalidOperationException("Only the officer who created this audit can update it.");
        }

        validateStatusTransition(record.getStatus(), request.getStatus());

        record.setStatus(request.getStatus());
        if (request.getFindings() != null) {
            record.setFindings(request.getFindings());
        }
        auditRecordRepository.save(record);

        auditLogService.log(officerId, AuditAction.AUDIT_STATUS_UPDATED, "AUDIT_RECORD",
                String.valueOf(auditId), "Status updated to: " + request.getStatus());

        return mapToResponse(record);
    }

    @Override
    @Transactional(readOnly = true)
    public AuditRecordResponse getById(Long auditId) {
        return mapToResponse(findById(auditId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditRecordResponse> getAll() {
        return auditRecordRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditRecordResponse> getByOfficer(Long officerId) {
        if (!userRepository.existsById(officerId)) {
            throw new ResourceNotFoundException("User", officerId);
        }
        return auditRecordRepository.findByOfficer_UserIdOrderByCreatedAtDesc(officerId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditRecordResponse> getByStatus(AuditStatus status) {
        return auditRecordRepository.findByStatusOrderByCreatedAtDesc(status)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private AuditRecord findById(Long auditId) {
        return auditRecordRepository.findById(auditId)
                .orElseThrow(() -> new ResourceNotFoundException("AuditRecord", auditId));
    }

    private void validateStatusTransition(AuditStatus current, AuditStatus next) {
        boolean valid = switch (current) {
            case OPEN      -> next == AuditStatus.IN_REVIEW;
            case IN_REVIEW -> next == AuditStatus.CLOSED;
            case CLOSED    -> false;
        };
        if (!valid) {
            throw new InvalidOperationException("Invalid audit status transition: " + current + " → " + next);
        }
    }

    private AuditRecordResponse mapToResponse(AuditRecord r) {
        return AuditRecordResponse.builder()
                .auditId(r.getAuditId())
                .officerId(r.getOfficer().getUserId())
                .officerName(r.getOfficer().getName())
                .scope(r.getScope())
                .findings(r.getFindings())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}

