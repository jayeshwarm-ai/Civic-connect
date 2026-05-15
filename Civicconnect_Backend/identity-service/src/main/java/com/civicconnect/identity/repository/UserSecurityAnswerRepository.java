package com.civicconnect.identity.repository;

import com.civicconnect.identity.entity.UserSecurityAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSecurityAnswerRepository extends JpaRepository<UserSecurityAnswer, Long> {
    List<UserSecurityAnswer> findByUser_UserId(Long userId);
    void deleteByUser_UserId(Long userId);
    long countByUser_UserId(Long userId);
}
