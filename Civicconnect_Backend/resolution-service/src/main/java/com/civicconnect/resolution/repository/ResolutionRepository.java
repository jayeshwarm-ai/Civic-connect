package com.civicconnect.resolution.repository;

import com.civicconnect.resolution.entity.Resolution;
import com.civicconnect.resolution.enums.ResolutionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResolutionRepository extends JpaRepository<Resolution, Long> {

    Optional<Resolution> findByRequestId(Long requestId);

    boolean existsByRequestId(Long requestId);

    List<Resolution> findByOfficerUserIdOrderByCreatedAtDesc(Long officerUserId);

    List<Resolution> findByStatusOrderByCreatedAtDesc(ResolutionStatus status);

    long countByStatus(ResolutionStatus status);
}
