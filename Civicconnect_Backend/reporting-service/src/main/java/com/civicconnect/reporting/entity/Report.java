package com.civicconnect.reporting.entity;

import com.civicconnect.reporting.enums.ReportScope;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Immutable report snapshot.
 * generatedByUserId → FK ref to identity-service (no JPA cross-service join).
 * metrics stores the aggregated string from all services.
 */
@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportScope scope;

    @Column(nullable = false, length = 5000)
    private String metrics;

    /** FK ref → identity-service users.userId */
    @Column(nullable = false)
    private Long generatedByUserId;

    /** Denormalised from identity-service */
    @Column(nullable = false)
    private String generatedByName;

    @Column(nullable = false, updatable = false)
    private LocalDateTime generatedDate;

    @PrePersist
    protected void onCreate() {
        generatedDate = LocalDateTime.now();
    }
}
