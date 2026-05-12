package com.civicconnect.compliance.entity;

import com.civicconnect.compliance.enums.AuditStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Audit record owned by compliance-service.
 * officerUserId → FK ref to identity-service (no JPA cross-service join).
 */
@Entity
@Table(name = "audit_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long auditId;

    /** FK ref → identity-service users.userId (COMPLIANCE_OFFICER) */
    @Column(nullable = false)
    private Long officerUserId;

    /** Denormalised from identity-service */
    @Column(nullable = false)
    private String officerName;

    @Column(nullable = false)
    private String scope;

    @Column(length = 2000)
    private String findings;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        status    = AuditStatus.OPEN;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
