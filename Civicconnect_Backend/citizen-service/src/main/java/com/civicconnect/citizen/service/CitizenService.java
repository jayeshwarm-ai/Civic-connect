package com.civicconnect.citizen.service;

import com.civicconnect.citizen.dto.request.CitizenRegistrationRequest;
import com.civicconnect.citizen.dto.request.DocumentVerificationRequest;
import com.civicconnect.citizen.dto.request.UpdateCitizenProfileRequest;
import com.civicconnect.citizen.dto.response.CitizenDocumentResponse;
import com.civicconnect.citizen.dto.response.CitizenResponse;
import com.civicconnect.citizen.enums.DocType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service interface for Citizen operations.
 */
public interface CitizenService {

    // ── Registration ──────────────────────────────────────────────────────────
    CitizenResponse registerCitizen(CitizenRegistrationRequest request);

    // ── Get Citizen ───────────────────────────────────────────────────────────
    CitizenResponse getCitizenById(Long citizenId);
    CitizenResponse getCitizenByUserId(Long userId);
    List<CitizenResponse> getAllCitizens();

    // ── Update Profile ────────────────────────────────────────────────────────
    CitizenResponse updateCitizenProfile(Long citizenId, UpdateCitizenProfileRequest request, Long requestingUserId);
    CitizenResponse updateMyProfile(UpdateCitizenProfileRequest request, Long userId);

    // ── Document Upload ───────────────────────────────────────────────────────
    CitizenDocumentResponse uploadDocument(Long citizenId, DocType docType, MultipartFile file, Long requestingUserId);
    CitizenDocumentResponse uploadMyDocument(DocType docType, MultipartFile file, Long userId);

    // ── Get Documents ─────────────────────────────────────────────────────────
    List<CitizenDocumentResponse> getDocumentsByCitizenId(Long citizenId);
    List<CitizenDocumentResponse> getMyDocuments(Long userId);
    List<CitizenDocumentResponse> getPendingDocuments();
    CitizenDocumentResponse getDocumentById(Long documentId);

    // ── Document Verification ─────────────────────────────────────────────────
    CitizenDocumentResponse verifyDocument(Long documentId, DocumentVerificationRequest request, Long officerId);

    // ── Deactivate Citizen ────────────────────────────────────────────────────
    void deactivateCitizen(Long citizenId, Long adminId);
}
