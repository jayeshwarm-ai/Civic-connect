package com.civicconnect.feedback.repository;

import com.civicconnect.feedback.entity.SatisfactionMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SatisfactionMetricRepository extends JpaRepository<SatisfactionMetric, Long> {

    Optional<SatisfactionMetric> findByOfficerUserId(Long officerUserId);

    boolean existsByOfficerUserId(Long officerUserId);

    /** Leaderboard ordered by highest average score */
    List<SatisfactionMetric> findAllByOrderByAverageScoreDesc();
}
