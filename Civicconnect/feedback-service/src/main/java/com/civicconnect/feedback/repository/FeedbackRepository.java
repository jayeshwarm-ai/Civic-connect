package com.civicconnect.feedback.repository;

import com.civicconnect.feedback.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    boolean existsByRequestId(Long requestId);

    Optional<Feedback> findByRequestId(Long requestId);

    List<Feedback> findByCitizenIdOrderByCreatedAtDesc(Long citizenId);
}
