package com.civicconnect.identity;

import com.civicconnect.identity.audit.AuditLogService;
import com.civicconnect.identity.dto.request.CreateStaffRequest;
import com.civicconnect.identity.dto.response.StaffResponse;
import com.civicconnect.identity.entity.User;
import com.civicconnect.identity.enums.Role;
import com.civicconnect.identity.enums.UserStatus;
import com.civicconnect.identity.exception.DuplicateResourceException;
import com.civicconnect.identity.exception.InvalidOperationException;
import com.civicconnect.identity.exception.ResourceNotFoundException;
import com.civicconnect.identity.repository.UserRepository;
import com.civicconnect.identity.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock private UserRepository  userRepository;
    @Mock private AuditLogService auditLogService;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private CreateStaffRequest createRequest;
    private User officerUser;

    @BeforeEach
    void setUp() {
        createRequest = new CreateStaffRequest();
        createRequest.setName("Officer Jane");
        createRequest.setEmail("jane@civic.com");
        createRequest.setPassword("secure123");
        createRequest.setPhone("9876543210");
        createRequest.setRole(Role.SERVICE_OFFICER);

        officerUser = User.builder()
                .userId(10L)
                .name("Officer Jane")
                .email("jane@civic.com")
                .phone("9876543210")
                .role(Role.SERVICE_OFFICER)
                .status(UserStatus.ACTIVE)
                .build();
    }

    // ── createStaff ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should create staff account successfully")
    void shouldCreateStaffSuccessfully() {
        when(userRepository.existsByEmail("jane@civic.com")).thenReturn(false);
        when(userRepository.existsByPhone("9876543210")).thenReturn(false);
        when(passwordEncoder.encode("secure123")).thenReturn("$2a$encoded");
        when(userRepository.save(any(User.class))).thenReturn(officerUser);

        StaffResponse response = userService.createStaff(createRequest, 1L);

        assertThat(response.getEmail()).isEqualTo("jane@civic.com");
        assertThat(response.getRole()).isEqualTo(Role.SERVICE_OFFICER);
        assertThat(response.getStatus()).isEqualTo(UserStatus.ACTIVE);
        verify(auditLogService).log(eq(1L), any(), eq("USER"), anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw InvalidOperationException when trying to create CITIZEN via this endpoint")
    void shouldThrowWhenCreatingCitizenViaStaffEndpoint() {
        createRequest.setRole(Role.CITIZEN);

        assertThatThrownBy(() -> userService.createStaff(createRequest, 1L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("CITIZEN accounts cannot be created");
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when email already exists")
    void shouldThrowOnDuplicateEmail() {
        when(userRepository.existsByEmail("jane@civic.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createStaff(createRequest, 1L))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email already registered");
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when phone already exists")
    void shouldThrowOnDuplicatePhone() {
        when(userRepository.existsByEmail("jane@civic.com")).thenReturn(false);
        when(userRepository.existsByPhone("9876543210")).thenReturn(true);

        assertThatThrownBy(() -> userService.createStaff(createRequest, 1L))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Phone already registered");
    }

    // ── getStaffByRole ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return staff list by role")
    void shouldReturnStaffByRole() {
        when(userRepository.findByRole(Role.SERVICE_OFFICER)).thenReturn(List.of(officerUser));

        List<StaffResponse> result = userService.getStaffByRole(Role.SERVICE_OFFICER);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRole()).isEqualTo(Role.SERVICE_OFFICER);
    }

    @Test
    @DisplayName("Should throw InvalidOperationException when querying CITIZEN role via staff endpoint")
    void shouldThrowWhenQueryingCitizenRole() {
        assertThatThrownBy(() -> userService.getStaffByRole(Role.CITIZEN))
                .isInstanceOf(InvalidOperationException.class);
    }

    // ── updateStaffStatus ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Should suspend staff account successfully")
    void shouldSuspendStaffSuccessfully() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(officerUser));
        when(userRepository.save(any(User.class))).thenReturn(officerUser);

        StaffResponse response = userService.updateStaffStatus(10L, UserStatus.SUSPENDED, 1L);

        assertThat(response).isNotNull();
        verify(userRepository).save(any(User.class));
        verify(auditLogService).log(eq(1L), any(), eq("USER"), anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw InvalidOperationException when status is already set")
    void shouldThrowWhenStatusAlreadySet() {
        officerUser.setStatus(UserStatus.ACTIVE);
        when(userRepository.findById(10L)).thenReturn(Optional.of(officerUser));

        assertThatThrownBy(() -> userService.updateStaffStatus(10L, UserStatus.ACTIVE, 1L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("already");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found")
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateStaffStatus(99L, UserStatus.SUSPENDED, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
