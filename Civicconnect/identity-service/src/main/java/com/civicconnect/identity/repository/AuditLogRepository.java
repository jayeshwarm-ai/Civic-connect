package com.civicconnect.identity.repository;

import com.civicconnect.identity.entity.AuditLog;
import com.civicconnect.identity.enums.AuditAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByPerformedByOrderByTimestampDesc(Long userId);

    List<AuditLog> findByResourceAndResourceId(String resource, String resourceId);

    List<AuditLog> findByAction(AuditAction action);
}
