package com.civicconnect.citizen.service.impl;

import com.civicconnect.citizen.dto.request.CitizenRegistrationRequest;
import com.civicconnect.citizen.dto.request.DocumentVerificationRequest;
import com.civicconnect.citizen.dto.request.UpdateCitizenProfileRequest;
import com.civicconnect.citizen.dto.response.CitizenDocumentResponse;
import com.civicconnect.citizen.dto.response.CitizenResponse;
import com.civicconnect.citizen.entity.Citizen;
import com.civicconnect.citizen.entity.CitizenDocument;
import com.civicconnect.citizen.enums.DocType;
import com.civicconnect.citizen.enums.UserStatus;
import com.civicconnect.citizen.enums.VerificationStatus;
import com.civicconnect.citizen.exception.DuplicateResourceException;
import com.civicconnect.citizen.exception.InvalidOperationException;
import com.civicconnect.citizen.exception.ResourceNotFoundException;
import com.civicconnect.citizen.feign.IdentityFeignClient;
import com.civicconnect.citizen.feign.NotificationFeignClient;
import com.civicconnect.citizen.feign.dto.AuditLogRequest;
import com.civicconnect.citizen.feign.dto.SendNotificationRequest;
import com.civicconnect.citizen.feign.dto.UserRegistrationRequest;
import com.civicconnect.citizen.feign.dto.UserValidationResponse;
import com.civicconnect.citizen.repository.CitizenDocumentRepository;
import com.civicconnect.citizen.repository.CitizenRepository;
import com.civicconnect.citizen.service.CitizenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CitizenServiceImpl implements CitizenService {

    private final CitizenRepository         citizenRepository;
    private final CitizenDocumentRepository documentRepository;
    private final IdentityFeignClient       identityFeignClient;
    private final NotificationFeignClient   notificationFeignClient;

    @Value("${file.upload.dir}")
    private String uploadDir;

    // ── 1. CITIZEN REGISTRATION ───────────────────────────────────────────────
    @Override
    @Transactional
    public CitizenResponse registerCitizen(CitizenRegistrationRequest request) {

        // Guard: email uniqueness (check locally first)
        if (citizenRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Email already registered: " + request.getEmail());
        }
        if (citizenRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateResourceException(
                    "Phone already registered: " + request.getPhone());
        }

        // Step 1: Create User in identity-service via Feign
        UserValidationResponse userResponse = identityFeignClient.registerUser(
                UserRegistrationRequest.builder()
                        .name(request.getName())
                        .email(request.getEmail())
                        .password(request.getPassword())
                        .phone(request.getPhone())
                        .build()
        );

        // Step 2: Create Citizen profile locally
        Citizen citizen = Citizen.builder()
                .userId(userResponse.getUserId())
                .name(request.getName())
                .dob(request.getDob())
                .gender(request.getGender())
                .address(request.getAddress())
                .contactInfo(request.getContactInfo())
                .email(request.getEmail())
                .phone(request.getPhone())
                .accountStatus(UserStatus.INACTIVE)
                .build();

        citizen = citizenRepository.save(citizen);

        // Step 3: Write audit log to identity-service
        writeAuditLog(userResponse.getUserId(), "CITIZEN_PROFILE_CREATED",
                "CITIZEN", String.valueOf(citizen.getCitizenId()),
                "Citizen profile created for userId: " + userResponse.getUserId());

        // Step 4: Notify all CITY_ADMINISTRATORs about new citizen registration
        notifyAdminsOnRegistration(citizen);

        return mapToResponse(citizen);
    }

    // ── 2. GET CITIZEN BY ID ──────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public CitizenResponse getCitizenById(Long citizenId) {
        return mapToResponse(findById(citizenId));
    }

    // ── 2b. GET ALL CITIZENS ───────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<CitizenResponse> getAllCitizens() {
        return citizenRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ── 3. GET CITIZEN BY USER ID ─────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public CitizenResponse getCitizenByUserId(Long userId) {
        Citizen citizen = citizenRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Citizen not found for userId: " + userId));
        return mapToResponse(citizen);
    }

    // ── 4. UPDATE CITIZEN PROFILE ─────────────────────────────────────────────
    @Override
    @Transactional
    public CitizenResponse updateCitizenProfile(Long citizenId,
                                                UpdateCitizenProfileRequest request,
                                                Long requestingUserId) {
        Citizen citizen = findById(citizenId);

        // Guard: citizen can only update their own profile
        if (!citizen.getUserId().equals(requestingUserId)) {
            throw new InvalidOperationException(
                    "You are not authorized to update this profile.");
        }

        boolean addressChanged = !citizen.getAddress().equals(request.getAddress());

        citizen.setAddress(request.getAddress());
        citizen.setContactInfo(request.getContactInfo());
        citizen.setPhone(request.getPhone());
        citizenRepository.save(citizen);

        if (addressChanged) {
            handleAddressChange(citizen, requestingUserId);
        }

        writeAuditLog(requestingUserId, "CITIZEN_PROFILE_UPDATED",
                "CITIZEN", String.valueOf(citizenId),
                addressChanged
                        ? "Profile updated: address changed — residence proof cleared for re-upload"
                        : "Profile updated: contact info changed");

        return mapToResponse(citizen);
    }

    // ── 4b. UPDATE MY PROFILE (for citizen) ─────────────────────────────────────
    @Override
    @Transactional
    public CitizenResponse updateMyProfile(UpdateCitizenProfileRequest request,
                                           Long userId) {
        Citizen citizen = citizenRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Citizen profile not found for userId: " + userId));

        boolean addressChanged = !citizen.getAddress().equals(request.getAddress());

        citizen.setAddress(request.getAddress());
        citizen.setContactInfo(request.getContactInfo());
        citizen.setPhone(request.getPhone());
        citizenRepository.save(citizen);

        // If the citizen changed their address, the previously verified residence
        // proof no longer matches their actual residence and must be re-uploaded.
        // We delete any existing PENDING / VERIFIED RESIDENCE_PROOF documents and
        // move the account back to INACTIVE so the citizen is prompted to re-upload.
        if (addressChanged) {
            handleAddressChange(citizen, userId);
        }

        writeAuditLog(userId, "CITIZEN_PROFILE_UPDATED",
                "CITIZEN", String.valueOf(citizen.getCitizenId()),
                addressChanged
                        ? "Profile updated: address changed — residence proof cleared for re-upload"
                        : "Profile updated: contact info changed");

        return mapToResponse(citizen);
    }

    // ── 5. UPLOAD DOCUMENT ────────────────────────────────────────────────────
    @Override
    @Transactional
    public CitizenDocumentResponse uploadDocument(Long citizenId,
                                                  DocType docType,
                                                  MultipartFile file,
                                                  Long requestingUserId) {
        Citizen citizen = findById(citizenId);

        // Guard: citizen can only upload their own documents
        if (!citizen.getUserId().equals(requestingUserId)) {
            throw new InvalidOperationException(
                    "You are not authorized to upload documents for this citizen.");
        }

        // Enforce: at most ONE non-rejected document per docType.
        // REJECTED documents don't block re-upload (otherwise citizen would
        // be stuck after a rejection). VERIFIED RESIDENCE_PROOF is cleared
        // by updateMyProfile when address changes.
        boolean blockingDocExists = documentRepository
                .findByCitizen_CitizenId(citizenId)
                .stream()
                .anyMatch(d -> d.getDocType() == docType
                        && d.getVerificationStatus() != VerificationStatus.REJECTED);
        if (blockingDocExists) {
            throw new InvalidOperationException(
                    docType.name().replace('_', ' ')
                    + " has already been uploaded and cannot be re-uploaded."
                    + (docType == DocType.RESIDENCE_PROOF
                            ? " To upload a new residence proof, update your address from your profile first."
                            : ""));
        }

        if (file.isEmpty()) {
            throw new InvalidOperationException("Uploaded file is empty.");
        }

        String fileUri = storeFile(file, citizenId, docType);

        CitizenDocument document = CitizenDocument.builder()
                .citizen(citizen)
                .docType(docType)
                .fileUri(fileUri)
                .build();
        document = documentRepository.save(document);

        writeAuditLog(requestingUserId, "DOCUMENT_UPLOADED",
                "DOCUMENT", String.valueOf(document.getDocumentId()),
                "DocType: " + docType + " uploaded for citizenId: " + citizenId);

        return mapToDocumentResponse(document);
    }

    // ── 5b. UPLOAD MY DOCUMENT (for citizen) ──────────────────────────────────
    @Override
    @Transactional
    public CitizenDocumentResponse uploadMyDocument(DocType docType,
                                                    MultipartFile file,
                                                    Long userId) {
        Citizen citizen = citizenRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Citizen profile not found for userId: " + userId));

        // Enforce: a citizen can have at most ONE non-rejected document of each
        // type (ID_PROOF, RESIDENCE_PROOF). REJECTED documents do not block a
        // re-upload — otherwise a citizen whose first attempt is rejected would
        // be stuck. If the existing document is PENDING or VERIFIED, the upload
        // is refused. To replace a VERIFIED RESIDENCE_PROOF the citizen must
        // first change their address, which clears the old RESIDENCE_PROOF
        // automatically (see updateMyProfile).
        boolean blockingDocExists = documentRepository
                .findByCitizen_CitizenId(citizen.getCitizenId())
                .stream()
                .anyMatch(d -> d.getDocType() == docType
                        && d.getVerificationStatus() != VerificationStatus.REJECTED);
        if (blockingDocExists) {
            throw new InvalidOperationException(
                    docType.name().replace('_', ' ')
                    + " has already been uploaded and cannot be re-uploaded."
                    + (docType == DocType.RESIDENCE_PROOF
                            ? " To upload a new residence proof, update your address from your profile first."
                            : ""));
        }

        if (file.isEmpty()) {
            throw new InvalidOperationException("Uploaded file is empty.");
        }

        String fileUri = storeFile(file, citizen.getCitizenId(), docType);

        CitizenDocument document = CitizenDocument.builder()
                .citizen(citizen)
                .docType(docType)
                .fileUri(fileUri)
                .build();
        document = documentRepository.save(document);

        writeAuditLog(userId, "DOCUMENT_UPLOADED",
                "DOCUMENT", String.valueOf(document.getDocumentId()),
                "DocType: " + docType + " uploaded for citizenId: " + citizen.getCitizenId());

        // Notify admins about new document upload
        notifyAdminsOnDocumentUpload(citizen, docType);

        return mapToDocumentResponse(document);
    }

    // ── 6. GET DOCUMENTS BY CITIZEN ───────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<CitizenDocumentResponse> getDocumentsByCitizenId(Long citizenId) {
        findById(citizenId); // verify exists
        return documentRepository.findByCitizen_CitizenId(citizenId)
                .stream()
                .map(this::mapToDocumentResponse)
                .collect(Collectors.toList());
    }

    // ── 6b. GET MY DOCUMENTS (for citizen) ────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<CitizenDocumentResponse> getMyDocuments(Long userId) {
        Citizen citizen = citizenRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Citizen profile not found for userId: " + userId));
        return documentRepository.findByCitizen_CitizenId(citizen.getCitizenId())
                .stream()
                .map(this::mapToDocumentResponse)
                .collect(Collectors.toList());
    }

    // ── 7. VERIFY / REJECT DOCUMENT ───────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public CitizenDocumentResponse getDocumentById(Long documentId) {
        CitizenDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", documentId));
        return mapToDocumentResponse(document);
    }

    @Override
    @Transactional
    public CitizenDocumentResponse verifyDocument(Long documentId,
                                                  DocumentVerificationRequest request,
                                                  Long officerId) {
        CitizenDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", documentId));

        // Guards
        if (document.getVerificationStatus() != VerificationStatus.PENDING) {
            throw new InvalidOperationException(
                    "Document is already " + document.getVerificationStatus()
                    + ". Only PENDING documents can be reviewed.");
        }
        if (request.getVerificationStatus() == VerificationStatus.REJECTED
                && (request.getRemarks() == null || request.getRemarks().isBlank())) {
            throw new InvalidOperationException(
                    "Remarks are required when rejecting a document.");
        }
        if (request.getVerificationStatus() == VerificationStatus.PENDING) {
            throw new InvalidOperationException(
                    "Cannot set verification status back to PENDING.");
        }

        // Validate officer exists via Feign
        UserValidationResponse officer = identityFeignClient.validateUser(officerId);
        if (!officer.isExists()) {
            throw new ResourceNotFoundException("Officer", officerId);
        }

        document.setVerificationStatus(request.getVerificationStatus());
        document.setRemarks(request.getRemarks());
        document.setReviewedByUserId(officerId);
        document.setReviewedAt(LocalDateTime.now());
        documentRepository.save(document);

        String auditAction = request.getVerificationStatus() == VerificationStatus.VERIFIED
                ? "DOCUMENT_VERIFIED" : "DOCUMENT_REJECTED";

        writeAuditLog(officerId, auditAction,
                "DOCUMENT", String.valueOf(documentId),
                "Document " + request.getVerificationStatus()
                + " for citizenId: " + document.getCitizen().getCitizenId());

        // Notify citizen about document verification result
        notifyCitizenOnDocumentReview(document, request.getVerificationStatus(), request.getRemarks());

        // If VERIFIED — check if all required docs are verified → activate user
        if (request.getVerificationStatus() == VerificationStatus.VERIFIED) {
            // Fetch fresh citizen object to ensure we have latest data
            Citizen freshCitizen = citizenRepository.findById(document.getCitizen().getCitizenId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Citizen", document.getCitizen().getCitizenId()));
            activateUserIfAllDocumentsVerified(freshCitizen, officerId);
        }

        return mapToDocumentResponse(document);
    }

    // ── 8. GET PENDING DOCUMENTS ──────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<CitizenDocumentResponse> getPendingDocuments() {
        return documentRepository.findByVerificationStatus(VerificationStatus.PENDING)
                .stream()
                .map(this::mapToDocumentResponse)
                .collect(Collectors.toList());
    }

    // ── 9. DEACTIVATE CITIZEN ─────────────────────────────────────────────────
    @Override
    @Transactional
    public void deactivateCitizen(Long citizenId, Long adminId) {
        Citizen citizen = findById(citizenId);

        if (citizen.getAccountStatus() == UserStatus.SUSPENDED) {
            throw new InvalidOperationException("Citizen account is already SUSPENDED.");
        }

        // Suspend user in identity-service
        identityFeignClient.suspendUser(citizen.getUserId());

        // Keep local status in sync
        citizen.setAccountStatus(UserStatus.SUSPENDED);
        citizenRepository.save(citizen);

        writeAuditLog(adminId, "USER_DEACTIVATED",
                "USER", String.valueOf(citizen.getUserId()),
                "Citizen account suspended by adminId: " + adminId);
    }

    // ── PRIVATE HELPERS ───────────────────────────────────────────────────────

    private void notifyAdminsOnRegistration(Citizen citizen) {
        try {
            List<Long> adminIds = identityFeignClient.getUserIdsByRole("CITY_ADMINISTRATOR");
            String message = "🆕 New citizen registered: " + citizen.getName()
                    + " (Email: " + citizen.getEmail() + ", CitizenID: #" + citizen.getCitizenId()
                    + "). Documents pending verification.";
            for (Long adminId : adminIds) {
                notificationFeignClient.sendNotification(
                        SendNotificationRequest.builder()
                                .userId(adminId)
                                .message(message)
                                .category("REQUEST")
                                .build());
            }
            log.info("Notified {} admin(s) about new citizen registration: {}",
                    adminIds.size(), citizen.getEmail());
        } catch (Exception e) {
            log.warn("Failed to notify admins on citizen registration: {}", e.getMessage());
        }
    }

    /**
     * Called when a citizen's address has actually changed. Any existing
     * non-rejected RESIDENCE_PROOF is deleted (the citizen will need to upload
     * a fresh proof matching the new address) and the account is moved back to
     * INACTIVE so the verification flow restarts. ID_PROOF is unaffected — it
     * doesn't depend on address. If the citizen was previously ACTIVE we also
     * notify them about the re-verification requirement.
     */
    private void handleAddressChange(Citizen citizen, Long actingUserId) {
        List<CitizenDocument> existingResidence = documentRepository
                .findByCitizen_CitizenId(citizen.getCitizenId())
                .stream()
                .filter(d -> d.getDocType() == DocType.RESIDENCE_PROOF
                        && d.getVerificationStatus() != VerificationStatus.REJECTED)
                .collect(Collectors.toList());

        if (existingResidence.isEmpty()) {
            // No residence proof yet (or only rejected ones). Nothing to clear.
            return;
        }

        documentRepository.deleteAll(existingResidence);

        boolean wasActive = citizen.getAccountStatus() == UserStatus.ACTIVE;
        if (wasActive) {
            // Move local + identity-service status back to INACTIVE
            citizen.setAccountStatus(UserStatus.INACTIVE);
            citizenRepository.save(citizen);
            try {
                identityFeignClient.deactivateUser(citizen.getUserId());
            } catch (Exception e) {
                log.warn("Failed to deactivate user in identity-service after address change: {}", e.getMessage());
            }
            try {
                notificationFeignClient.sendNotification(
                        SendNotificationRequest.builder()
                                .userId(citizen.getUserId())
                                .message("Your address has been updated. Please upload a new Residence Proof to re-verify your account.")
                                .category("REQUEST")
                                .build());
            } catch (Exception e) {
                log.warn("Failed to notify citizen about residence re-upload: {}", e.getMessage());
            }
        }

        writeAuditLog(actingUserId, "RESIDENCE_PROOF_CLEARED",
                "CITIZEN", String.valueOf(citizen.getCitizenId()),
                "Address changed — " + existingResidence.size()
                        + " residence proof document(s) cleared for re-upload");
    }

    private void notifyAdminsOnDocumentUpload(Citizen citizen, DocType docType) {
        try {
            List<Long> adminIds = identityFeignClient.getUserIdsByRole("CITY_ADMINISTRATOR");
            String message = "📄 New document uploaded by " + citizen.getName()
                    + " (CitizenID: #" + citizen.getCitizenId() + "): " + docType.name()
                    + ". Please review and verify.";
            for (Long adminId : adminIds) {
                notificationFeignClient.sendNotification(
                        SendNotificationRequest.builder()
                                .userId(adminId)
                                .message(message)
                                .category("REQUEST")
                                .build());
            }
        } catch (Exception e) {
            log.warn("Failed to notify admins on document upload: {}", e.getMessage());
        }
    }

    private void notifyCitizenOnDocumentReview(CitizenDocument document,
                                                VerificationStatus status, String remarks) {
        try {
            Citizen citizen = document.getCitizen();
            String emoji = status == VerificationStatus.VERIFIED ? "✅" : "❌";
            String message = emoji + " Your document (" + document.getDocType().name()
                    + ") has been " + status.name() + ".";
            if (status == VerificationStatus.REJECTED && remarks != null) {
                message += " Reason: " + remarks;
            }
            notificationFeignClient.sendNotification(
                    SendNotificationRequest.builder()
                            .userId(citizen.getUserId())
                            .message(message)
                            .category("REQUEST")
                            .build());
        } catch (Exception e) {
            log.warn("Failed to notify citizen on document review: {}", e.getMessage());
        }
    }

    private void activateUserIfAllDocumentsVerified(Citizen citizen, Long officerId) {
        List<CitizenDocument> allDocs =
                documentRepository.findByCitizen_CitizenId(citizen.getCitizenId());

        boolean hasVerifiedId = allDocs.stream()
                .anyMatch(d -> d.getDocType() == DocType.ID_PROOF
                        && d.getVerificationStatus() == VerificationStatus.VERIFIED);

        boolean hasVerifiedResidence = allDocs.stream()
                .anyMatch(d -> d.getDocType() == DocType.RESIDENCE_PROOF
                        && d.getVerificationStatus() == VerificationStatus.VERIFIED);

        boolean anyPending = allDocs.stream()
                .anyMatch(d -> d.getVerificationStatus() == VerificationStatus.PENDING);

        if (hasVerifiedId && hasVerifiedResidence && !anyPending) {
            // Activate in identity-service via Feign
            identityFeignClient.activateUser(citizen.getUserId());

            // Sync local status
            citizen.setAccountStatus(UserStatus.ACTIVE);
            citizenRepository.save(citizen);

            // Notify citizen that account is activated
            try {
                notificationFeignClient.sendNotification(
                        SendNotificationRequest.builder()
                                .userId(citizen.getUserId())
                                .message("🎉 Your CivicConnect account has been activated! All documents verified. You can now access all citizen services.")
                                .category("REQUEST")
                                .build());
            } catch (Exception e) {
                log.warn("Failed to notify citizen on activation: {}", e.getMessage());
            }

            log.info("Citizen {} (userId={}) account ACTIVATED after document verification",
                    citizen.getCitizenId(), citizen.getUserId());
        }
    }

    private Citizen findById(Long citizenId) {
        return citizenRepository.findById(citizenId)
                .orElseThrow(() -> new ResourceNotFoundException("Citizen", citizenId));
    }

    private String storeFile(MultipartFile file, Long citizenId, DocType docType) {
        try {
            String filename = citizenId + "_" + docType.name() + "_"
                    + UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path filePath = uploadPath.resolve(filename).normalize();
            file.transferTo(filePath.toFile());
            return filePath.toString();
        } catch (IOException e) {
            throw new InvalidOperationException("Failed to store file: " + e.getMessage());
        }
    }

    private void writeAuditLog(Long performedBy, String action,
                                String resource, String resourceId, String detail) {
        try {
            identityFeignClient.writeAuditLog(AuditLogRequest.builder()
                    .performedBy(performedBy)
                    .action(action)
                    .resource(resource)
                    .resourceId(resourceId)
                    .detail(detail)
                    .build());
        } catch (Exception e) {
            // Audit log failure must never break the main flow
            log.warn("Failed to write audit log: action={}, resource={}: {}",
                    action, resource, e.getMessage());
        }
    }

    // ── MAPPERS ───────────────────────────────────────────────────────────────

    private CitizenResponse mapToResponse(Citizen c) {
        return CitizenResponse.builder()
                .citizenId(c.getCitizenId())
                .userId(c.getUserId())
                .name(c.getName())
                .dob(c.getDob())
                .gender(c.getGender())
                .address(c.getAddress())
                .contactInfo(c.getContactInfo())
                .email(c.getEmail())
                .phone(c.getPhone())
                .accountStatus(c.getAccountStatus())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    private CitizenDocumentResponse mapToDocumentResponse(CitizenDocument d) {
        return CitizenDocumentResponse.builder()
                .documentId(d.getDocumentId())
                .citizenId(d.getCitizen().getCitizenId())
                .docType(d.getDocType())
                .fileUri(d.getFileUri())
                .uploadedDate(d.getUploadedDate())
                .verificationStatus(d.getVerificationStatus())
                .remarks(d.getRemarks())
                .reviewedByUserId(d.getReviewedByUserId())
                .reviewedAt(d.getReviewedAt())
                .build();
    }
}

