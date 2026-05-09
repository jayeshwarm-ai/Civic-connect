package com.civicconnect.resolution.repository;

import com.civicconnect.resolution.entity.WorkflowStep;
import com.civicconnect.resolution.enums.WorkflowStepStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowStepRepository extends JpaRepository<WorkflowStep, Long> {

    List<WorkflowStep> findByResolution_ResolutionIdOrderByCreatedAtAsc(Long resolutionId);

    /** Returns true if ANY step under this resolution is not yet COMPLETED */
    boolean existsByResolution_ResolutionIdAndStatusNot(
            Long resolutionId, WorkflowStepStatus status);
}
