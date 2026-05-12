package com.civicconnect.compliance.repository;

import com.civicconnect.compliance.entity.AuditRecord;
import com.civicconnect.compliance.enums.AuditStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditRecordRepository extends JpaRepository<AuditRecord, Long> {

    List<AuditRecord> findByOfficerUserIdOrderByCreatedAtDesc(Long officerUserId);

    List<AuditRecord> findByStatusOrderByCreatedAtDesc(AuditStatus status);

    long countByStatus(AuditStatus status);
}
