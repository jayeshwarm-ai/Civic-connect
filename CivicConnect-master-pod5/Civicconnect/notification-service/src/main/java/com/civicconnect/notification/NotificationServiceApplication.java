package com.civicconnect.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * CivicConnect – Notification Service (Port: 8088)
 *
 * Responsibilities:
 *  - Receives notification sends from ALL other services via POST /internal/notifications/send
 *  - Persists notifications in civicconnect_notification DB
 *  - Provides users access to their own notifications (read/dismiss/filter)
 *
 * Internal API (no JWT, called by all other services via Feign):
 *   POST /internal/notifications/send  → receives userId + message + category + optional requestId
 *
 * Public API (JWT required, all roles):
 *   GET  /api/v1/notifications          → all notifications for userId
 *   GET  /api/v1/notifications/unread   → unread only
 *   GET  /api/v1/notifications/unread/count
 *   GET  /api/v1/notifications/category
 *   PATCH /api/v1/notifications/{id}/read
 *   PATCH /api/v1/notifications/{id}/dismiss
 *
 * Feign clients:
 *  - identity-service → validate userId before querying notifications
 *
 * DB: civicconnect_notification
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
