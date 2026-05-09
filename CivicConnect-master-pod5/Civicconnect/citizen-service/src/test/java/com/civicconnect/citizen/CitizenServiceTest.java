package com.civicconnect.citizen;

import com.civicconnect.citizen.dto.request.CitizenRegistrationRequest;
import com.civicconnect.citizen.dto.request.DocumentVerificationRequest;
import com.civicconnect.citizen.dto.request.UpdateCitizenProfileRequest;
import com.civicconnect.citizen.dto.response.CitizenDocumentResponse;
import com.civicconnect.citizen.dto.response.CitizenResponse;
import com.civicconnect.citizen.entity.Citizen;
import com.civicconnect.citizen.entity.CitizenDocument;
import com.civicconnect.citizen.enums.DocType;
import com.civicconnect.citizen.enums.Gender;
import com.civicconnect.citizen.enums.UserStatus;
import com.civicconnect.citizen.enums.VerificationStatus;
import com.civicconnect.citizen.exception.DuplicateResourceException;
import com.civicconnect.citizen.exception.InvalidOperationException;
import com.civicconnect.citizen.exception.ResourceNotFoundException;
import com.civicconnect.citizen.feign.IdentityFeignClient;
import com.civicconnect.citizen.feign.dto.UserValidationResponse;
import com.civicconnect.citizen.feign.dto.UserRegistrationRequest;
import com.civicconnect.citizen.repository.CitizenDocumentRepository;
import com.civicconnect.citizen.repository.CitizenRepository;
import com.civicconnect.citizen.service.CitizenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CitizenService Tests")
class CitizenServiceTest {

    @Mock private CitizenRepository         citizenRepository;
    @Mock private CitizenDocumentRepository documentRepository;
    @Mock private IdentityFeignClient       identityFeignClient;

    @InjectMocks
    private CitizenService citizenService;

    private Citizen activeCitizen;
    private CitizenDocument pendingIdDoc;
    private CitizenDocument pendingResidenceDoc;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(citizenService, "uploadDir", "/tmp/test-uploads");

        activeCitizen = Citizen.builder()
                .citizenId(1L)
                .userId(10L)
                .name("Alice Citizen")
                .email("alice@civic.com")
                .phone("9876543210")
                .dob(LocalDate.of(1990, 1, 1))
                .gender(Gender.FEMALE)
                .address("123 Main St")
                .contactInfo("alice@civic.com")
                .accountStatus(UserStatus.INACTIVE)
                .build();

        pendingIdDoc = CitizenDocument.builder()
                .documentId(1L)
                .citizen(activeCitizen)
                .docType(DocType.ID_PROOF)
                .fileUri("/tmp/id.pdf")
                .verificationStatus(VerificationStatus.PENDING)
                .build();

        pendingResidenceDoc = CitizenDocument.builder()
                .documentId(2L)
                .citizen(activeCitizen)
                .docType(DocType.RESIDENCE_PROOF)
                .fileUri("/tmp/residence.pdf")
                .verificationStatus(VerificationStatus.PENDING)
                .build();
    }

    // ── registerCitizen ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Should register citizen successfully")
    void shouldRegisterCitizenSuccessfully() {
        CitizenRegistrationRequest request = buildRegistrationRequest();

        when(citizenRepository.existsByEmail("alice@civic.com")).thenReturn(false);
        when(citizenRepository.existsByPhone("9876543210")).thenReturn(false);
        when(identityFeignClient.registerUser(any(UserRegistrationRequest.class)))
                .thenReturn(UserValidationResponse.builder()
                        .userId(10L).exists(true).build());
        when(citizenRepository.save(any(Citizen.class))).thenReturn(activeCitizen);
        doNothing().when(identityFeignClient).writeAuditLog(any());

        CitizenResponse response = citizenService.registerCitizen(request);

        assertThat(response.getCitizenId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("alice@civic.com");
        assertThat(response.getAccountStatus()).isEqualTo(UserStatus.INACTIVE);
        verify(citizenRepository).save(any(Citizen.class));
        verify(identityFeignClient).registerUser(any());
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when email already registered")
    void shouldThrowOnDuplicateEmail() {
        CitizenRegistrationRequest request = buildRegistrationRequest();
        when(citizenRepository.existsByEmail("alice@civic.com")).thenReturn(true);

        assertThatThrownBy(() -> citizenService.registerCitizen(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email already registered");
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when phone already registered")
    void shouldThrowOnDuplicatePhone() {
        CitizenRegistrationRequest request = buildRegistrationRequest();
        when(citizenRepository.existsByEmail("alice@civic.com")).thenReturn(false);
        when(citizenRepository.existsByPhone("9876543210")).thenReturn(true);

        assertThatThrownBy(() -> citizenService.registerCitizen(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Phone already registered");
    }

    // ── getCitizenById ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return citizen by ID")
    void shouldReturnCitizenById() {
        when(citizenRepository.findById(1L)).thenReturn(Optional.of(activeCitizen));

        CitizenResponse response = citizenService.getCitizenById(1L);

        assertThat(response.getCitizenId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Alice Citizen");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when citizen not found")
    void shouldThrowWhenCitizenNotFound() {
        when(citizenRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> citizenService.getCitizenById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Citizen not found with id: 99");
    }

    // ── updateCitizenProfile ──────────────────────────────────────────────────

    @Test
    @DisplayName("Should update citizen profile when correct user")
    void shouldUpdateProfileSuccessfully() {
        UpdateCitizenProfileRequest request = new UpdateCitizenProfileRequest();
        request.setAddress("456 New St");
        request.setContactInfo("alice_new@civic.com");
        request.setPhone("9111111111");

        when(citizenRepository.findById(1L)).thenReturn(Optional.of(activeCitizen));
        when(citizenRepository.save(any())).thenReturn(activeCitizen);
        doNothing().when(identityFeignClient).writeAuditLog(any());

        CitizenResponse response = citizenService.updateCitizenProfile(1L, request, 10L);

        assertThat(response).isNotNull();
        verify(citizenRepository).save(any(Citizen.class));
    }

    @Test
    @DisplayName("Should throw InvalidOperationException when wrong user tries to update profile")
    void shouldThrowWhenWrongUserUpdatesProfile() {
        UpdateCitizenProfileRequest request = new UpdateCitizenProfileRequest();
        request.setAddress("456 New St");
        request.setContactInfo("other@civic.com");
        request.setPhone("9111111111");

        when(citizenRepository.findById(1L)).thenReturn(Optional.of(activeCitizen));

        // userId 99 is not the owner (owner is 10L)
        assertThatThrownBy(() -> citizenService.updateCitizenProfile(1L, request, 99L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("not authorized");
    }

    // ── verifyDocument ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should verify document successfully")
    void shouldVerifyDocumentSuccessfully() {
        DocumentVerificationRequest request = new DocumentVerificationRequest();
        request.setVerificationStatus(VerificationStatus.VERIFIED);

        when(documentRepository.findById(1L)).thenReturn(Optional.of(pendingIdDoc));
        when(identityFeignClient.validateUser(5L))
                .thenReturn(UserValidationResponse.builder().userId(5L).exists(true).build());
        when(documentRepository.save(any())).thenReturn(pendingIdDoc);
        when(documentRepository.findByCitizen_CitizenId(1L))
                .thenReturn(List.of(pendingIdDoc, pendingResidenceDoc));
        doNothing().when(identityFeignClient).writeAuditLog(any());

        CitizenDocumentResponse response = citizenService.verifyDocument(1L, request, 5L);

        assertThat(response).isNotNull();
        verify(documentRepository).save(any(CitizenDocument.class));
    }

    @Test
    @DisplayName("Should throw InvalidOperationException when document already reviewed")
    void shouldThrowWhenDocumentAlreadyReviewed() {
        pendingIdDoc.setVerificationStatus(VerificationStatus.VERIFIED);
        DocumentVerificationRequest request = new DocumentVerificationRequest();
        request.setVerificationStatus(VerificationStatus.REJECTED);
        request.setRemarks("Wrong document");

        when(documentRepository.findById(1L)).thenReturn(Optional.of(pendingIdDoc));

        assertThatThrownBy(() -> citizenService.verifyDocument(1L, request, 5L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("already VERIFIED");
    }

    @Test
    @DisplayName("Should throw InvalidOperationException when rejecting without remarks")
    void shouldThrowWhenRejectingWithoutRemarks() {
        DocumentVerificationRequest request = new DocumentVerificationRequest();
        request.setVerificationStatus(VerificationStatus.REJECTED);
        request.setRemarks(null);   // No remarks provided

        when(documentRepository.findById(1L)).thenReturn(Optional.of(pendingIdDoc));

        assertThatThrownBy(() -> citizenService.verifyDocument(1L, request, 5L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Remarks are required");
    }

    @Test
    @DisplayName("Should activate user when both documents are verified")
    void shouldActivateUserWhenBothDocumentsVerified() {
        DocumentVerificationRequest request = new DocumentVerificationRequest();
        request.setVerificationStatus(VerificationStatus.VERIFIED);

        CitizenDocument verifiedResidence = CitizenDocument.builder()
                .documentId(2L)
                .citizen(activeCitizen)
                .docType(DocType.RESIDENCE_PROOF)
                .verificationStatus(VerificationStatus.VERIFIED)
                .build();

        CitizenDocument verifiedId = CitizenDocument.builder()
                .documentId(1L)
                .citizen(activeCitizen)
                .docType(DocType.ID_PROOF)
                .verificationStatus(VerificationStatus.VERIFIED)
                .build();

        when(documentRepository.findById(1L)).thenReturn(Optional.of(pendingIdDoc));
        when(identityFeignClient.validateUser(5L))
                .thenReturn(UserValidationResponse.builder().userId(5L).exists(true).build());
        when(documentRepository.save(any())).thenReturn(verifiedId);
        // Both docs are now verified, none pending
        when(documentRepository.findByCitizen_CitizenId(1L))
                .thenReturn(List.of(verifiedId, verifiedResidence));
        when(citizenRepository.save(any())).thenReturn(activeCitizen);
        doNothing().when(identityFeignClient).activateUser(anyLong());
        doNothing().when(identityFeignClient).writeAuditLog(any());

        citizenService.verifyDocument(1L, request, 5L);

        verify(identityFeignClient).activateUser(eq(10L));
        verify(citizenRepository).save(any(Citizen.class));
    }

    // ── deactivateCitizen ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Should deactivate citizen successfully")
    void shouldDeactivateCitizenSuccessfully() {
        when(citizenRepository.findById(1L)).thenReturn(Optional.of(activeCitizen));
        doNothing().when(identityFeignClient).suspendUser(10L);
        when(citizenRepository.save(any())).thenReturn(activeCitizen);
        doNothing().when(identityFeignClient).writeAuditLog(any());

        citizenService.deactivateCitizen(1L, 99L);

        verify(identityFeignClient).suspendUser(10L);
        verify(citizenRepository).save(any(Citizen.class));
    }

    @Test
    @DisplayName("Should throw InvalidOperationException when citizen already suspended")
    void shouldThrowWhenCitizenAlreadySuspended() {
        activeCitizen.setAccountStatus(UserStatus.SUSPENDED);
        when(citizenRepository.findById(1L)).thenReturn(Optional.of(activeCitizen));

        assertThatThrownBy(() -> citizenService.deactivateCitizen(1L, 99L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("already SUSPENDED");
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private CitizenRegistrationRequest buildRegistrationRequest() {
        CitizenRegistrationRequest req = new CitizenRegistrationRequest();
        req.setName("Alice Citizen");
        req.setEmail("alice@civic.com");
        req.setPassword("password123");
        req.setPhone("9876543210");
        req.setDob(LocalDate.of(1990, 1, 1));
        req.setGender(Gender.FEMALE);
        req.setAddress("123 Main St");
        req.setContactInfo("alice@civic.com");
        return req;
    }
}
