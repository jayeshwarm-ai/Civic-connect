package com.civicconnect.citizen.controller;

import com.civicconnect.citizen.dto.request.CitizenRegistrationRequest;
import com.civicconnect.citizen.dto.request.DocumentVerificationRequest;
import com.civicconnect.citizen.dto.request.UpdateCitizenProfileRequest;
import com.civicconnect.citizen.dto.response.CitizenDocumentResponse;
import com.civicconnect.citizen.dto.response.CitizenResponse;
import com.civicconnect.citizen.enums.DocType;
import com.civicconnect.citizen.service.CitizenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/v1/citizens")
@RequiredArgsConstructor
@Tag(name = "Citizen Management",
     description = "Registration, profile management, and document verification")
@SecurityRequirement(name = "BearerAuth")
public class CitizenController {

    private final CitizenService citizenService;

    // ── POST /api/v1/citizens/register — PUBLIC ───────────────────────────────
    @Operation(
        summary = "Register citizen",
        description = "Public endpoint. Creates a User (INACTIVE) and Citizen profile. "
                    + "Citizen must upload documents and get them verified before account becomes ACTIVE."
    )
    @ApiResponse(responseCode = "201", description = "Citizen registered successfully")
    @ApiResponse(responseCode = "409", description = "Email or phone already registered")
    @PostMapping("/register")
    public ResponseEntity<CitizenResponse> registerCitizen(
            @Valid @RequestBody CitizenRegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(citizenService.registerCitizen(request));
    }

    // ── GET /api/v1/citizens — ADMIN only ─────────────────────────────────────
    @Operation(summary = "Get all citizens — CITY_ADMINISTRATOR only")
    @ApiResponse(responseCode = "200", description = "All citizens returned")
    @PreAuthorize("hasRole('CITY_ADMINISTRATOR')")
    @GetMapping
    public ResponseEntity<List<CitizenResponse>> getAllCitizens() {
        return ResponseEntity.ok(citizenService.getAllCitizens());
    }

    // ── GET /api/v1/citizens/{citizenId} ──────────────────────────────────────
    @Operation(summary = "Get citizen by ID")
    @ApiResponse(responseCode = "200", description = "Citizen found")
    @ApiResponse(responseCode = "404", description = "Citizen not found")
    @PreAuthorize("hasAnyRole('CITIZEN','SERVICE_OFFICER','DEPARTMENT_HEAD','CITY_ADMINISTRATOR')")
    @GetMapping("/{citizenId}")
    public ResponseEntity<CitizenResponse> getCitizenById(@PathVariable Long citizenId) {
        return ResponseEntity.ok(citizenService.getCitizenById(citizenId));
    }

    // ── GET /api/v1/citizens/by-user/{userId} ─────────────────────────────────
    @Operation(summary = "Get citizen by user ID")
    @ApiResponse(responseCode = "200", description = "Citizen found")
    @ApiResponse(responseCode = "404", description = "Citizen not found for this userId")
    @PreAuthorize("hasAnyRole('CITIZEN','SERVICE_OFFICER','DEPARTMENT_HEAD','CITY_ADMINISTRATOR')")
    @GetMapping("/by-user/{userId}")
    public ResponseEntity<CitizenResponse> getCitizenByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(citizenService.getCitizenByUserId(userId));
    }

    // ── GET /api/v1/citizens/my-profile — CITIZEN only ──────────────────────────
    @Operation(
        summary = "Get my profile — CITIZEN only",
        description = "Returns the logged-in citizen's profile. citizenId is auto-resolved from JWT."
    )
    @ApiResponse(responseCode = "200", description = "Profile found")
    @ApiResponse(responseCode = "404", description = "Citizen profile not found")
    @PreAuthorize("hasRole('CITIZEN')")
    @GetMapping("/my-profile")
    public ResponseEntity<CitizenResponse> getMyProfile(Authentication authentication) {
        return ResponseEntity.ok(
                citizenService.getCitizenByUserId(extractUserId(authentication)));
    }

    // ── PUT /api/v1/citizens/my-profile — CITIZEN only ──────────────────────────
    @Operation(
        summary = "Update my profile — CITIZEN only",
        description = "Citizen can update their own address, contactInfo, and phone. "
                    + "citizenId is auto-resolved from JWT."
    )
    @ApiResponse(responseCode = "200", description = "Profile updated")
    @PreAuthorize("hasRole('CITIZEN')")
    @PutMapping("/my-profile")
    public ResponseEntity<CitizenResponse> updateMyProfile(
            @Valid @RequestBody UpdateCitizenProfileRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(
                citizenService.updateMyProfile(request, extractUserId(authentication)));
    }


    // ── POST /api/v1/citizens/my-documents — CITIZEN only ─────────────────────
    @Operation(
        summary = "Upload my document — CITIZEN only",
        description = "Upload ID_PROOF or RESIDENCE_PROOF. citizenId is auto-resolved from JWT. "
                    + "Stored with PENDING status."
    )
    @ApiResponse(responseCode = "201", description = "Document uploaded")
    @ApiResponse(responseCode = "400", description = "Empty file or account not INACTIVE")
    @PreAuthorize("hasRole('CITIZEN')")
    @PostMapping(value = "/my-documents",
                 consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CitizenDocumentResponse> uploadMyDocument(
            @Parameter(description = "ID_PROOF or RESIDENCE_PROOF")
            @RequestParam("docType") DocType docType,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(citizenService.uploadMyDocument(docType, file,
                        extractUserId(authentication)));
    }

    // ── GET /api/v1/citizens/my-documents — CITIZEN only ──────────────────────
    @Operation(
        summary = "Get my documents — CITIZEN only",
        description = "Returns all documents uploaded by the logged-in citizen. "
                    + "citizenId is auto-resolved from JWT."
    )
    @ApiResponse(responseCode = "200", description = "Documents returned")
    @PreAuthorize("hasRole('CITIZEN')")
    @GetMapping("/my-documents")
    public ResponseEntity<List<CitizenDocumentResponse>> getMyDocuments(
            Authentication authentication) {
        return ResponseEntity.ok(
                citizenService.getMyDocuments(extractUserId(authentication)));
    }


    // ── GET /api/v1/citizens/{citizenId}/documents ────────────────────────────
    @Operation(summary = "Get all documents for a citizen")
    @ApiResponse(responseCode = "200", description = "Documents returned")
    @PreAuthorize("hasAnyRole('CITIZEN','SERVICE_OFFICER','DEPARTMENT_HEAD','CITY_ADMINISTRATOR')")
    @GetMapping("/{citizenId}/documents")
    public ResponseEntity<List<CitizenDocumentResponse>> getDocuments(
            @PathVariable Long citizenId) {
        return ResponseEntity.ok(citizenService.getDocumentsByCitizenId(citizenId));
    }

    // ── GET /api/v1/citizens/documents/pending — Officer+ ─────────────────────
    @Operation(summary = "Get all PENDING documents — SERVICE_OFFICER and above")
    @ApiResponse(responseCode = "200", description = "Pending documents returned")
    @PreAuthorize("hasAnyRole('SERVICE_OFFICER','DEPARTMENT_HEAD','CITY_ADMINISTRATOR')")
    @GetMapping("/documents/pending")
    public ResponseEntity<List<CitizenDocumentResponse>> getPendingDocuments() {
        return ResponseEntity.ok(citizenService.getPendingDocuments());
    }

    // ── PATCH /api/v1/citizens/documents/{documentId}/verify — Officer+ ────────
    @Operation(
        summary = "Verify or reject document — SERVICE_OFFICER and above",
        description = "Set status to VERIFIED or REJECTED. Remarks are required for rejection. "
                    + "Verifying all required documents auto-activates the citizen's account."
    )
    @ApiResponse(responseCode = "200", description = "Document reviewed")
    @ApiResponse(responseCode = "400", description = "Already reviewed or remarks missing")
    @ApiResponse(responseCode = "404", description = "Document not found")
    @PreAuthorize("hasAnyRole('SERVICE_OFFICER','DEPARTMENT_HEAD','CITY_ADMINISTRATOR')")
    @PatchMapping("/documents/{documentId}/verify")
    public ResponseEntity<CitizenDocumentResponse> verifyDocument(
            @PathVariable Long documentId,
            @Valid @RequestBody DocumentVerificationRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(
                citizenService.verifyDocument(documentId, request,
                        extractUserId(authentication)));
    }

    // ── GET /api/v1/citizens/documents/{documentId}/download — Officer+ ────────
    @Operation(summary = "Download document file — SERVICE_OFFICER and above")
    @ApiResponse(responseCode = "200", description = "File returned")
    @ApiResponse(responseCode = "404", description = "Document or file not found")
    @PreAuthorize("hasAnyRole('CITIZEN','SERVICE_OFFICER','DEPARTMENT_HEAD','CITY_ADMINISTRATOR')")
    @GetMapping("/documents/{documentId}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long documentId) {
        CitizenDocumentResponse doc = citizenService.getDocumentById(documentId);
        try {
            Path filePath = Paths.get(doc.getFileUri()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            String filename = filePath.getFileName().toString();
            String contentType = filename.endsWith(".pdf") ? "application/pdf"
                    : filename.endsWith(".png") ? "image/png"
                    : filename.endsWith(".jpg") || filename.endsWith(".jpeg") ? "image/jpeg"
                    : "application/octet-stream";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ── PATCH /api/v1/citizens/{citizenId}/deactivate — ADMIN only ────────────
    @Operation(summary = "Deactivate citizen account — CITY_ADMINISTRATOR only")
    @ApiResponse(responseCode = "204", description = "Account deactivated")
    @ApiResponse(responseCode = "400", description = "Already suspended")
    @PreAuthorize("hasRole('CITY_ADMINISTRATOR')")
    @PatchMapping("/{citizenId}/deactivate")
    public ResponseEntity<Void> deactivateCitizen(
            @PathVariable Long citizenId,
            Authentication authentication) {
        citizenService.deactivateCitizen(citizenId, extractUserId(authentication));
        return ResponseEntity.noContent().build();
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    private Long extractUserId(Authentication auth) {
        return ((Number) ((UsernamePasswordAuthenticationToken) auth).getDetails()).longValue();
    }
}
