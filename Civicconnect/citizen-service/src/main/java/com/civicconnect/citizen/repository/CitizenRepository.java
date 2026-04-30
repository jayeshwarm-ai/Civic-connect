package com.civicconnect.citizen.repository;

import com.civicconnect.citizen.entity.Citizen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CitizenRepository extends JpaRepository<Citizen, Long> {

    Optional<Citizen> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);
}
