package com.civicconnect.identity;

import com.civicconnect.identity.audit.AuditLogService;
import com.civicconnect.identity.dto.request.LoginRequest;
import com.civicconnect.identity.dto.response.LoginResponse;
import com.civicconnect.identity.entity.User;
import com.civicconnect.identity.enums.Role;
import com.civicconnect.identity.enums.UserStatus;
import com.civicconnect.identity.exception.InvalidOperationException;
import com.civicconnect.identity.exception.ResourceNotFoundException;
import com.civicconnect.identity.repository.UserRepository;
import com.civicconnect.identity.security.JwtUtil;
import com.civicconnect.identity.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock private UserRepository  userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil         jwtUtil;
    @Mock private AuditLogService auditLogService;

    @InjectMocks
    private AuthService authService;

    private User activeUser;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        activeUser = User.builder()
                .userId(1L)
                .name("John Citizen")
                .email("john@civic.com")
                .password("$2a$encoded")
                .phone("9876543210")
                .role(Role.CITIZEN)
                .status(UserStatus.ACTIVE)
                .build();

        loginRequest = new LoginRequest();
        loginRequest.setEmail("john@civic.com");
        loginRequest.setPassword("password123");
    }

    // ── Success path ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return JWT on valid credentials")
    void shouldReturnJwtOnValidCredentials() {
        when(userRepository.findByEmail("john@civic.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("password123", "$2a$encoded")).thenReturn(true);
        when(jwtUtil.generateToken(1L, "john@civic.com", Role.CITIZEN)).thenReturn("mock.jwt.token");

        LoginResponse response = authService.login(loginRequest);

        assertThat(response.getToken()).isEqualTo("mock.jwt.token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getRole()).isEqualTo(Role.CITIZEN);
        verify(auditLogService).log(eq(1L), any(), eq("USER"), eq("1"), anyString());
    }

    // ── Email not found ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Should throw ResourceNotFoundException when email not found")
    void shouldThrowWhenEmailNotFound() {
        when(userRepository.findByEmail("unknown@civic.com")).thenReturn(Optional.empty());
        loginRequest.setEmail("unknown@civic.com");

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Invalid credentials");
    }

    // ── Wrong password ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should throw InvalidOperationException on wrong password")
    void shouldThrowOnWrongPassword() {
        when(userRepository.findByEmail("john@civic.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("password123", "$2a$encoded")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("incorrect password");
    }

    // ── Suspended account ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Should throw InvalidOperationException when account is SUSPENDED")
    void shouldThrowWhenAccountSuspended() {
        activeUser.setStatus(UserStatus.SUSPENDED);
        when(userRepository.findByEmail("john@civic.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("password123", "$2a$encoded")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("SUSPENDED");
    }

    // ── INACTIVE account can still login ──────────────────────────────────────

    @Test
    @DisplayName("Should allow login for INACTIVE account (documents not yet verified)")
    void shouldAllowLoginForInactiveAccount() {
        activeUser.setStatus(UserStatus.INACTIVE);
        when(userRepository.findByEmail("john@civic.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("password123", "$2a$encoded")).thenReturn(true);
        when(jwtUtil.generateToken(anyLong(), anyString(), any())).thenReturn("token");

        LoginResponse response = authService.login(loginRequest);

        assertThat(response.getToken()).isEqualTo("token");
    }

    // ── Officer login ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return correct role for SERVICE_OFFICER login")
    void shouldReturnCorrectRoleForOfficer() {
        activeUser.setRole(Role.SERVICE_OFFICER);
        when(userRepository.findByEmail("john@civic.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(jwtUtil.generateToken(1L, "john@civic.com", Role.SERVICE_OFFICER))
                .thenReturn("officer.token");

        LoginResponse response = authService.login(loginRequest);

        assertThat(response.getRole()).isEqualTo(Role.SERVICE_OFFICER);
        assertThat(response.getToken()).isEqualTo("officer.token");
    }
}
