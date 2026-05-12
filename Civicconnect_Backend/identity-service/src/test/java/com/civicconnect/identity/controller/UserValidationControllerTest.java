package com.civicconnect.identity.controller;

import com.civicconnect.identity.dto.response.UserValidationResponse;
import com.civicconnect.identity.entity.User;
import com.civicconnect.identity.enums.Role;
import com.civicconnect.identity.enums.UserStatus;
import com.civicconnect.identity.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserValidationController Tests")
class UserValidationControllerTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserValidationController controller;

    private User activeCitizen;

    @BeforeEach
    void setUp() {
        activeCitizen = User.builder()
                .userId(100L).name("Jane").email("jane@example.com")
                .role(Role.CITIZEN).status(UserStatus.ACTIVE).build();
    }

    @Test
    @DisplayName("validateUser → 200 exists=true when found")
    void validateUser_existing() {
        when(userRepository.findById(100L)).thenReturn(Optional.of(activeCitizen));

        ResponseEntity<UserValidationResponse> resp = controller.validateUser(100L);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().isExists()).isTrue();
        assertThat(resp.getBody().getUserId()).isEqualTo(100L);
        assertThat(resp.getBody().getName()).isEqualTo("Jane");
    }

    @Test
    @DisplayName("validateUser → 200 exists=false when not found")
    void validateUser_missing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<UserValidationResponse> resp = controller.validateUser(99L);

        assertThat(resp.getBody().isExists()).isFalse();
        assertThat(resp.getBody().getUserId()).isEqualTo(99L);
    }

    @Test
    @DisplayName("existsById → true / false")
    void existsById_bothCases() {
        when(userRepository.existsById(100L)).thenReturn(true);
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThat(controller.existsById(100L).getBody()).isTrue();
        assertThat(controller.existsById(99L).getBody()).isFalse();
    }

    @Test
    @DisplayName("validateByEmail → 200 exists=true when found")
    void validateByEmail_found() {
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(activeCitizen));

        ResponseEntity<UserValidationResponse> resp = controller.validateByEmail("jane@example.com");

        assertThat(resp.getBody().isExists()).isTrue();
        assertThat(resp.getBody().getEmail()).isEqualTo("jane@example.com");
    }

    @Test
    @DisplayName("validateByEmail → 200 exists=false when not found")
    void validateByEmail_notFound() {
        when(userRepository.findByEmail("nope@example.com")).thenReturn(Optional.empty());

        ResponseEntity<UserValidationResponse> resp = controller.validateByEmail("nope@example.com");

        assertThat(resp.getBody().isExists()).isFalse();
    }

    @Test
    @DisplayName("getUserIdsByRole → 200 with list of userIds")
    void getUserIdsByRole_returnsIds() {
        User u1 = User.builder().userId(1L).role(Role.COMPLIANCE_OFFICER).build();
        User u2 = User.builder().userId(2L).role(Role.COMPLIANCE_OFFICER).build();
        when(userRepository.findByRole(Role.COMPLIANCE_OFFICER)).thenReturn(List.of(u1, u2));

        ResponseEntity<List<Long>> resp = controller.getUserIdsByRole(Role.COMPLIANCE_OFFICER);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).containsExactly(1L, 2L);
    }

    @Test
    @DisplayName("registerCitizenUser → 201 with INACTIVE CITIZEN")
    void registerCitizenUser_creates() {
        UserValidationController.UserRegistrationInternalRequest req =
                new UserValidationController.UserRegistrationInternalRequest();
        req.setName("Jane");
        req.setEmail("new@example.com");
        req.setPassword("plaintext");
        req.setPhone("9999");

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("plaintext")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setUserId(101L);
            return u;
        });

        ResponseEntity<UserValidationResponse> resp = controller.registerCitizenUser(req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody().getUserId()).isEqualTo(101L);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getRole()).isEqualTo(Role.CITIZEN);
        assertThat(saved.getStatus()).isEqualTo(UserStatus.INACTIVE);
        assertThat(saved.getPassword()).isEqualTo("hashed");
    }

    @Test
    @DisplayName("registerCitizenUser → 409 when email already exists")
    void registerCitizenUser_duplicateEmail() {
        UserValidationController.UserRegistrationInternalRequest req =
                new UserValidationController.UserRegistrationInternalRequest();
        req.setEmail("taken@example.com");
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        ResponseEntity<UserValidationResponse> resp = controller.registerCitizenUser(req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("activateUser → 204, flips to ACTIVE")
    void activateUser_flipsStatus() {
        User inactive = User.builder().userId(100L).status(UserStatus.INACTIVE).build();
        when(userRepository.findById(100L)).thenReturn(Optional.of(inactive));

        ResponseEntity<Void> resp = controller.activateUser(100L);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(inactive.getStatus()).isEqualTo(UserStatus.ACTIVE);
        verify(userRepository).save(inactive);
    }

    @Test
    @DisplayName("activateUser → 204 no-op when user not found")
    void activateUser_missing() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseEntity<Void> resp = controller.activateUser(999L);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("suspendUser → 204, flips to SUSPENDED")
    void suspendUser_flipsStatus() {
        User active = User.builder().userId(100L).status(UserStatus.ACTIVE).build();
        when(userRepository.findById(100L)).thenReturn(Optional.of(active));

        ResponseEntity<Void> resp = controller.suspendUser(100L);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(active.getStatus()).isEqualTo(UserStatus.SUSPENDED);
        verify(userRepository).save(active);
    }
}
