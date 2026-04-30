package com.civicconnect.resolution.entity;

import com.civicconnect.resolution.enums.ResolutionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * In microservices mode:
 *  - requestId         → FK ref to service-request-service (no JPA relationship)
 *  - officerUserId     → FK ref to identity-service (no JPA relationship)
 *
 * Names are denormalised at write-time to avoid cross-service reads on every GET.
 */
@Entity
@Table(name = "resolutions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resolution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long resolutionId;

    /** FK ref → service-request-service — one request has exactly one resolution */
    @Column(nullable = false, unique = true)
    private Long requestId;

    /** FK ref → identity-service users.userId (SERVICE_OFFICER) */
    @Column(nullable = false)
    private Long officerUserId;

    /** Denormalised from identity-service at create time */
    @Column(nullable = false)
    private String officerName;

    /** citizenUserId — stored for notification targeting without extra Feign call */
    @Column(nullable = false)
    private Long citizenUserId;

    @Column(nullable = false, length = 1000)
    private String actions;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResolutionStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        status    = ResolutionStatus.IN_PROGRESS;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
