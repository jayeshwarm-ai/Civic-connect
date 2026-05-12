package com.civicconnect.servicerequest.entity;

import com.civicconnect.servicerequest.enums.ServiceRequestStatus;
import com.civicconnect.servicerequest.enums.ServiceRequestType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * In microservices mode:
 *  - citizenId  → FK ref to citizen-service (no JPA relationship)
 *  - assignedOfficerUserId → FK ref to identity-service (no JPA relationship)
 *
 * citizenName and assignedOfficerName are denormalised at write-time
 * to avoid cross-service calls on every read.
 */
@Entity
@Table(name = "service_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requestId;

    /** FK ref → citizen-service citizens.citizenId */
    @Column(nullable = false)
    private Long citizenId;

    /** Denormalised from citizen-service at submit time */
    @Column(nullable = false)
    private String citizenName;

    /** FK ref → identity-service users.userId (SERVICE_OFFICER) */
    @Column
    private Long assignedOfficerUserId;

    /** Denormalised from identity-service at assign time */
    @Column
    private String assignedOfficerName;

    /** userId of the citizen (from JWT) — used for ownership checks */
    @Column(nullable = false)
    private Long citizenUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceRequestType type;

    @Column(nullable = false, length = 1000)
    private String description;

    /** Indian state, e.g. "Tamil Nadu". Selected from a fixed list on the frontend. */
    @Column(nullable = false, length = 60)
    private String state;

    /** City within the selected state, e.g. "Chennai". */
    @Column(nullable = false, length = 80)
    private String city;

    /** Free-form street address typed by the citizen. */
    @Column(nullable = false, length = 255)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceRequestStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        status    = ServiceRequestStatus.SUBMITTED;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
