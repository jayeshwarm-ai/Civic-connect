package com.civicconnect.citizen.entity;

import com.civicconnect.citizen.enums.Gender;
import com.civicconnect.citizen.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * In microservices mode, Citizen no longer holds a direct JPA @OneToOne to User.
 * Instead it stores userId as a plain Long FK reference.
 * Cross-service user data is fetched via IdentityFeignClient when needed.
 */
@Entity
@Table(name = "citizens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Citizen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long citizenId;

    /** FK reference to User in identity-service (not a JPA relationship) */
    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDate dob;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(nullable = false)
    private String contactInfo;

    /** Denormalised from identity-service for performance */
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus accountStatus;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (accountStatus == null) accountStatus = UserStatus.INACTIVE;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
