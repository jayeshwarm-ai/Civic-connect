package com.civicconnect.compliance.entity;

import com.civicconnect.compliance.enums.ComplianceResult;
import com.civicconnect.compliance.enums.ComplianceType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Immutable compliance record.
 * entityId refers to a ServiceRequest ID (type=REQUEST) or Resolution ID (type=RESOLUTION).
 * createdByUserId → FK ref to identity-service (no JPA cross-service join).
 */
@Entity
@Table(name = "compliance_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplianceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long complianceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ComplianceType type;

    /** ID of the ServiceRequest or Resolution being checked */
    @Column(nullable = false)
    private Long entityId;

    /** FK ref → identity-service users.userId (COMPLIANCE_OFFICER) */
    @Column(nullable = false)
    private Long createdByUserId;

    /** Denormalised from identity-service */
    @Column(nullable = false)
    private String createdByName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ComplianceResult result;

    @Column(nullable = false, length = 1000)
    private String notes;

    /** Immutable — no updatedAt */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
