package com.civicconnect.feedback.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * In microservices mode:
 *  - requestId      → FK ref to service-request-service
 *  - citizenId      → FK ref to citizen-service
 *  - citizenUserId  → FK ref to identity-service (for audit)
 *
 * Names denormalised at write time for read performance.
 */
@Entity
@Table(name = "feedbacks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long feedbackId;

    /** FK ref → service-request-service — one feedback per closed request */
    @Column(nullable = false, unique = true)
    private Long requestId;

    /** FK ref → citizen-service */
    @Column(nullable = false)
    private Long citizenId;

    /** Denormalised from citizen-service */
    @Column(nullable = false)
    private String citizenName;

    /** userId of the citizen — from JWT, used for audit */
    @Column(nullable = false)
    private Long citizenUserId;

    /** FK ref → identity-service (assigned officer) */
    @Column
    private Long officerUserId;

    /** Rating: 1 to 5 */
    @Column(nullable = false)
    private Integer rating;

    @Column(length = 1000)
    private String comments;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
