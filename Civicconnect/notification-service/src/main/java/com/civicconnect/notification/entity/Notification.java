package com.civicconnect.notification.entity;

import com.civicconnect.notification.enums.NotificationCategory;
import com.civicconnect.notification.enums.NotificationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * In microservices mode:
 *  - userId    → FK ref to identity-service (no JPA cross-service join)
 *  - requestId → FK ref to service-request-service (nullable, no JPA join)
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    /** FK ref → identity-service users.userId (the recipient) */
    @Column(nullable = false)
    private Long userId;

    /** FK ref → service-request-service (nullable — not all notifications are request-related) */
    @Column
    private Long requestId;

    @Column(nullable = false, length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        status      = NotificationStatus.UNREAD;
    }
}
