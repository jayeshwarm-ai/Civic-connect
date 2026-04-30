package com.civicconnect.feedback.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Tracks rolling satisfaction score per SERVICE_OFFICER.
 * officerUserId → FK ref to identity-service (no JPA relationship).
 * officerName denormalised at create/update time.
 */
@Entity
@Table(name = "satisfaction_metrics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SatisfactionMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long metricId;

    /** FK ref → identity-service users.userId (SERVICE_OFFICER), unique per officer */
    @Column(nullable = false, unique = true)
    private Long officerUserId;

    /** Denormalised from identity-service */
    @Column(nullable = false)
    private String officerName;

    @Column(nullable = false)
    private Long totalRatingSum;

    @Column(nullable = false)
    private Long totalFeedbackCount;

    @Column(nullable = false)
    private Double averageScore;

    @Column(nullable = false)
    private LocalDateTime lastUpdatedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastUpdatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdatedAt = LocalDateTime.now();
    }
}
