package com.civicconnect.identity.entity;

import com.civicconnect.identity.enums.AuditStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * A formal compliance audit initiated by a COMPLIANCE_OFFICER.
 * Different from AuditLog (system audit trail) — this is a human-driven audit process.
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

    /** Compliance Officer who initiated the audit */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "officer_id", nullable = false)
    private User officer;

    /** Scope of the audit, e.g. "Road requests Q1 2026" */
    @Column(nullable = false)
    private String scope;

    /** Findings recorded during or after the audit */
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
        status = AuditStatus.OPEN;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
