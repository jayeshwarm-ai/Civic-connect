package com.civicconnect.identity;

import com.civicconnect.identity.audit.AuditLogService;
import com.civicconnect.identity.dto.response.AuditLogResponse;
import com.civicconnect.identity.dto.response.UserResponse;
import com.civicconnect.identity.entity.AuditLog;
import com.civicconnect.identity.entity.User;
import com.civicconnect.identity.enums.AuditAction;
import com.civicconnect.identity.enums.Role;
import com.civicconnect.identity.enums.UserStatus;
import com.civicconnect.identity.exception.InvalidOperationException;
import com.civicconnect.identity.exception.ResourceNotFoundException;
import com.civicconnect.identity.repository.AuditLogRepository;
import com.civicconnect.identity.repository.UserRepository;
import com.civicconnect.identity.service.IAMService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("IAMService Tests")
class IAMServiceTest {

    @Mock private UserRepository     userRepository;
    @Mock private AuditLogRepository auditLogRepository;
    @Mock private AuditLogService    auditLogService;

    @InjectMocks
    private IAMService iamService;

    private User adminUser;
    private User citizenUser;

    @BeforeEach
    void setUp() {
        adminUser = User.builder()
                .userId(1L)
                .name("Admin")
                .email("admin@civic.com")
                .phone("9000000001")
                .role(Role.CITY_ADMINISTRATOR)
                .status(UserStatus.ACTIVE)
                .build();

        citizenUser = User.builder()
                .userId(5L)
                .name("Citizen Bob")
                .email("bob@civic.com")
                .phone("9000000005")
                .role(Role.CITIZEN)
                .status(UserStatus.ACTIVE)
                .build();
    }

    // ── getMyProfile ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return profile for authenticated user")
    void shouldReturnProfile() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        UserResponse response = iamService.getMyProfile(1L);

        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getRole()).isEqualTo(Role.CITY_ADMINISTRATOR);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found")
    void shouldThrowWhenUserNotFoundForProfile() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> iamService.getMyProfile(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
    }

    // ── getAuditLogsByUser ────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return audit logs for user")
    void shouldReturnAuditLogsForUser() {
        AuditLog log = AuditLog.builder()
                .auditId(1L)
                .performedBy(5L)
                .action(AuditAction.USER_LOGIN)
                .resource("USER")
                .resourceId("5")
                .detail("Login")
                .build();

        when(userRepository.existsById(5L)).thenReturn(true);
        when(auditLogService.getLogsForUser(5L)).thenReturn(List.of(log));

        List<AuditLogResponse> logs = iamService.getAuditLogsByUser(5L);

        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getAction()).isEqualTo(AuditAction.USER_LOGIN);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user does not exist for audit logs")
    void shouldThrowWhenUserNotFoundForAuditLogs() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> iamService.getAuditLogsByUser(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── deactivateUser ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should deactivate an ACTIVE user successfully")
    void shouldDeactivateUser() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(citizenUser));
        when(userRepository.save(any(User.class))).thenReturn(citizenUser);

        UserResponse response = iamService.deactivateUser(5L, 1L);

        assertThat(response).isNotNull();
        verify(userRepository).save(any());
        verify(auditLogService).log(eq(1L), eq(AuditAction.USER_DEACTIVATED),
                eq("USER"), eq("5"), anyString());
    }

    @Test
    @DisplayName("Should throw InvalidOperationException when user is already INACTIVE")
    void shouldThrowWhenAlreadyInactive() {
        citizenUser.setStatus(UserStatus.INACTIVE);
        when(userRepository.findById(5L)).thenReturn(Optional.of(citizenUser));

        assertThatThrownBy(() -> iamService.deactivateUser(5L, 1L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("already INACTIVE");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when target user not found")
    void shouldThrowWhenTargetUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> iamService.deactivateUser(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── getAuditLogsByAction ──────────────────────────────────────────────────

    @Test
    @DisplayName("Should return audit logs filtered by action")
    void shouldReturnAuditLogsByAction() {
        AuditLog log = AuditLog.builder()
                .auditId(2L)
                .performedBy(1L)
                .action(AuditAction.STAFF_CREATED)
                .resource("USER")
                .resourceId("10")
                .detail("Staff created")
                .build();

        when(auditLogRepository.findByAction(AuditAction.STAFF_CREATED))
                .thenReturn(List.of(log));

        List<AuditLogResponse> result = iamService.getAuditLogsByAction(AuditAction.STAFF_CREATED);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAction()).isEqualTo(AuditAction.STAFF_CREATED);
    }
}
