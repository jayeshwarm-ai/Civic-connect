package com.civicconnect.identity.repository;

import com.civicconnect.identity.entity.SecurityQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecurityQuestionRepository extends JpaRepository<SecurityQuestion, Long> {
    boolean existsByQuestionText(String questionText);
}
