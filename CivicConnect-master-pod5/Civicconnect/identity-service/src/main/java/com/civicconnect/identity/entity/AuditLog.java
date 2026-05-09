package com.civicconnect.identity.entity;

import com.civicconnect.identity.enums.AuditAction;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Immutable audit trail record.
 * Every significant action across ALL microservices is written here.
 * No setters — written once, never modified.
 */
@Entity
@Table(name = "audit_logs")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long auditId;

    /** userId of the actor who triggered the action */
    @Column(nullable = false)
    private Long performedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;

    /** Entity type affected: "USER", "CITIZEN", "SERVICE_REQUEST", etc. */
    @Column(nullable = false)
    private String resource;

    /** Primary key of the affected entity */
    @Column(nullable = false)
    private String resourceId;

    /** Optional context or description */
    @Column(length = 1000)
    private String detail;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}
