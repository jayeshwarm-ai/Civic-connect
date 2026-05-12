package com.civicconnect.identity.audit;

import com.civicconnect.identity.entity.AuditLog;
import com.civicconnect.identity.enums.AuditAction;
import com.civicconnect.identity.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Internal audit trail writer.
 * Called by every service layer within identity-service
 * whenever a significant action is performed.
 */
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void log(Long performedBy, AuditAction action,
                    String resource, String resourceId, String detail) {
        AuditLog log = AuditLog.builder()
                .performedBy(performedBy)
                .action(action)
                .resource(resource)
                .resourceId(resourceId)
                .detail(detail)
                .build();
        auditLogRepository.save(log);
    }

    public List<AuditLog> getLogsForUser(Long userId) {
        return auditLogRepository.findByPerformedByOrderByTimestampDesc(userId);
    }

    public List<AuditLog> getLogsForResource(String resource, String resourceId) {
        return auditLogRepository.findByResourceAndResourceId(resource, resourceId);
    }
}
