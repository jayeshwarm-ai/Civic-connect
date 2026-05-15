package com.civicconnect.identity.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * One row per (user, question) pair. Stores the BCrypt hash of the user's
 * answer (normalized lowercase+trimmed before hashing). A citizen has
 * exactly 3 of these rows for password recovery via security questions.
 */
@Entity
@Table(name = "user_security_answers",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "question_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSecurityAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long answerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private SecurityQuestion question;

    @Column(nullable = false, length = 100)
    private String answerHash;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
