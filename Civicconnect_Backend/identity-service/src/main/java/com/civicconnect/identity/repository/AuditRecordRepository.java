package com.civicconnect.identity.repository;

import com.civicconnect.identity.entity.AuditRecord;
import com.civicconnect.identity.enums.AuditStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditRecordRepository extends JpaRepository<AuditRecord, Long> {

    List<AuditRecord> findByOfficer_UserIdOrderByCreatedAtDesc(Long officerId);

    List<AuditRecord> findByStatusOrderByCreatedAtDesc(AuditStatus status);

    long countByStatus(AuditStatus status);
}
