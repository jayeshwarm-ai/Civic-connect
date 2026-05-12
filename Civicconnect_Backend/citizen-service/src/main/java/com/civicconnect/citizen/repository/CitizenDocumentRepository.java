package com.civicconnect.citizen.repository;

import com.civicconnect.citizen.entity.CitizenDocument;
import com.civicconnect.citizen.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CitizenDocumentRepository extends JpaRepository<CitizenDocument, Long> {

    List<CitizenDocument> findByCitizen_CitizenId(Long citizenId);

    List<CitizenDocument> findByVerificationStatus(VerificationStatus status);

    long countByCitizen_CitizenIdAndVerificationStatus(Long citizenId, VerificationStatus status);
}
