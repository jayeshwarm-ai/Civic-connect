package com.civicconnect.compliance.repository;

import com.civicconnect.compliance.entity.ComplianceRecord;
import com.civicconnect.compliance.enums.ComplianceResult;
import com.civicconnect.compliance.enums.ComplianceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplianceRecordRepository extends JpaRepository<ComplianceRecord, Long> {

    List<ComplianceRecord> findByTypeAndEntityIdOrderByCreatedAtDesc(ComplianceType type, Long entityId);

    List<ComplianceRecord> findByResultOrderByCreatedAtDesc(ComplianceResult result);

    List<ComplianceRecord> findByCreatedByUserIdOrderByCreatedAtDesc(Long officerUserId);

    List<ComplianceRecord> findByTypeOrderByCreatedAtDesc(ComplianceType type);

    long countByResult(ComplianceResult result);
}
