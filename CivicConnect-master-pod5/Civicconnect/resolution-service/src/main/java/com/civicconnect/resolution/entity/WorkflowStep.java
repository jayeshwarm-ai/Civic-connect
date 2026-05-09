package com.civicconnect.resolution.entity;

import com.civicconnect.resolution.enums.WorkflowStepStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * assignedToUserId → FK ref to identity-service (no JPA relationship)
 * assignedToUserName denormalised at create time.
 */
@Entity
@Table(name = "workflow_steps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stepId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolution_id", nullable = false)
    private Resolution resolution;

    @Column(nullable = false)
    private String description;

    /** FK ref → identity-service users.userId */
    @Column(nullable = false)
    private Long assignedToUserId;

    /** Denormalised from identity-service at add time */
    @Column(nullable = false)
    private String assignedToUserName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkflowStepStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        status    = WorkflowStepStatus.PENDING;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
