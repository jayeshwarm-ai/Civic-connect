package com.civicconnect.notification.service;

import com.civicconnect.notification.dto.request.SendNotificationRequest;
import com.civicconnect.notification.dto.response.NotificationResponse;
import com.civicconnect.notification.enums.NotificationCategory;

import java.util.List;

/**
 * Service interface for Notification operations.
 */
public interface NotificationService {
    void sendNotification(SendNotificationRequest request);

    // ── 1. GET ALL NOTIFICATIONS FOR A USER ───────────────────────────────────
    List<NotificationResponse> getNotificationsByUserId(Long userId);

    // ── 2. GET UNREAD NOTIFICATIONS ────────────────────────────────────────────
    List<NotificationResponse> getUnreadNotifications(Long userId);

    // ── 3. GET UNREAD COUNT ────────────────────────────────────────────────────
    long getUnreadCount(Long userId);

    // ── 4. MARK AS READ ────────────────────────────────────────────────────────
    NotificationResponse markAsRead(Long notificationId, Long userId);

    // ── 5. DISMISS NOTIFICATION ────────────────────────────────────────────────
    NotificationResponse dismissNotification(Long notificationId, Long userId);

    // ── 6. GET BY CATEGORY ─────────────────────────────────────────────────────
    List<NotificationResponse> getNotificationsByCategory(Long userId, NotificationCategory category);
}
