package com.civicconnect.identity.controller;

import com.civicconnect.identity.dto.response.UserValidationResponse;
import com.civicconnect.identity.entity.User;
import com.civicconnect.identity.enums.Role;
import com.civicconnect.identity.enums.UserStatus;
import com.civicconnect.identity.repository.UserRepository;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Internal-only API consumed by other microservices via Feign clients.
 *
 * NOT in Swagger (@Hidden). NOT behind JWT. Accessible service-to-service only.
 *
 * Endpoints:
 *   GET    /internal/users/{userId}           → validate user + get role/status
 *   GET    /internal/users/{userId}/exists    → lightweight boolean check
 *   GET    /internal/users/by-email           → lookup by email
 *   POST   /internal/users/register           → citizen-service creates a CITIZEN user here
 *   PATCH  /internal/users/{userId}/activate  → citizen-service activates after doc verification
 *   PATCH  /internal/users/{userId}/suspend   → citizen-service suspends deactivated citizens
 */
@Hidden
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class UserValidationController {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    // ── GET /{userId} ─────────────────────────────────────────────────────────
    @GetMapping("/{userId}")
    public ResponseEntity<UserValidationResponse> validateUser(@PathVariable Long userId) {
        return userRepository.findById(userId)
                .map(user -> ResponseEntity.ok(mapToValidation(user, true)))
                .orElseGet(() -> ResponseEntity.ok(
                        UserValidationResponse.builder()
                                .userId(userId)
                                .exists(false)
                                .build()));
    }

    // ── GET /{userId}/exists ──────────────────────────────────────────────────
    @GetMapping("/{userId}/exists")
    public ResponseEntity<Boolean> existsById(@PathVariable Long userId) {
        return ResponseEntity.ok(userRepository.existsById(userId));
    }

    // ── GET /by-email ─────────────────────────────────────────────────────────
    @GetMapping("/by-email")
    public ResponseEntity<UserValidationResponse> validateByEmail(@RequestParam String email) {
        return userRepository.findByEmail(email)
                .map(user -> ResponseEntity.ok(mapToValidation(user, true)))
                .orElseGet(() -> ResponseEntity.ok(
                        UserValidationResponse.builder()
                                .exists(false)
                                .build()));
    }

    // ── GET /by-role?role=CITY_ADMINISTRATOR ─────────────────────────────────
    @GetMapping("/by-role")
    public ResponseEntity<List<Long>> getUserIdsByRole(@RequestParam Role role) {
        List<Long> userIds = userRepository.findByRole(role)
                .stream()
                .map(User::getUserId)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userIds);
    }

    // ── POST /register — called by citizen-service during self-registration ───
    /**
     * Creates a CITIZEN user with INACTIVE status.
     * Called exclusively by citizen-service when a citizen self-registers.
     * Password is hashed here before saving.
     */
    @PostMapping("/register")
    public ResponseEntity<UserValidationResponse> registerCitizenUser(
            @RequestBody UserRegistrationInternalRequest request) {

        // Duplicate check (citizen-service also checks locally, this is the source of truth)
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(Role.CITIZEN)
                .status(UserStatus.INACTIVE)
                .build();

        user = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapToValidation(user, true));
    }

    // ── POST /{userId}/activate — called by citizen-service after doc verification
    /**
     * Activates the user account after all documents are verified.
     * Called by citizen-service when both ID_PROOF + RESIDENCE_PROOF are VERIFIED.
     */
    @PostMapping("/{userId}/activate")
    public ResponseEntity<Void> activateUser(@PathVariable Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setStatus(UserStatus.ACTIVE);
            userRepository.save(user);
        });
        return ResponseEntity.noContent().build();
    }

    // ── POST /{userId}/suspend — called by citizen-service on deactivation ───
    /**
     * Suspends a citizen account.
     * Called by citizen-service when admin deactivates a citizen.
     */
    @PostMapping("/{userId}/suspend")
    public ResponseEntity<Void> suspendUser(@PathVariable Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setStatus(UserStatus.SUSPENDED);
            userRepository.save(user);
        });
        return ResponseEntity.noContent().build();
    }

    // ── Inner DTO ─────────────────────────────────────────────────────────────
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserRegistrationInternalRequest {
        private String name;
        private String email;
        private String password;
        private String phone;
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    private UserValidationResponse mapToValidation(User user, boolean exists) {
        return UserValidationResponse.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .exists(exists)
                .build();
    }
}
