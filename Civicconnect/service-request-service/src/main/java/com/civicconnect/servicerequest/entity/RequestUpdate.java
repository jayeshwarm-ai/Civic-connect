package com.civicconnect.servicerequest.entity;

import com.civicconnect.servicerequest.enums.ServiceRequestStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Immutable audit trail for a ServiceRequest.
 * Each status change creates one RequestUpdate record.
 * officerUserId → FK ref to identity-service (no JPA relationship)
 */
@Entity
@Table(name = "request_updates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestUpdate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long updateId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private ServiceRequest serviceRequest;

    /** FK ref → identity-service users.userId */
    @Column(nullable = false)
    private Long officerUserId;

    /** Denormalised officer name */
    @Column(nullable = false)
    private String officerName;

    @Column(nullable = false, length = 1000)
    private String notes;

    /** Status at the time of this update */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceRequestStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
