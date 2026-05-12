package com.civicconnect.notification.controller;

import com.civicconnect.notification.dto.request.SendNotificationRequest;
import com.civicconnect.notification.dto.response.NotificationResponse;
import com.civicconnect.notification.enums.NotificationCategory;
import com.civicconnect.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Notification Management",
     description = "Every authenticated user can view and manage their own notifications")
@SecurityRequirement(name = "BearerAuth")
public class NotificationController {

    private final NotificationService notificationService;

    private static final String ALL_ROLES =
            "hasAnyRole('CITIZEN','SERVICE_OFFICER','DEPARTMENT_HEAD','CITY_ADMINISTRATOR','COMPLIANCE_OFFICER')";

    // ── INTERNAL: POST /internal/notifications/send ───────────────────────────
    /**
     * Called by ALL other microservices via Feign (no JWT required).
     * This is the single entry point for every notification in the system.
     */
    @Hidden
    @PostMapping("/internal/notifications/send")
    public ResponseEntity<Void> sendNotification(@RequestBody SendNotificationRequest request) {
        notificationService.sendNotification(request);
        return ResponseEntity.ok().build();
    }

    // ── GET /api/v1/notifications?userId= ────────────────────────────────────
    @Operation(summary = "Get all notifications for a user")
    @ApiResponse(responseCode = "200", description = "Notifications returned")
    @PreAuthorize(ALL_ROLES)
    @GetMapping("/api/v1/notifications")
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @RequestParam Long userId) {
        return ResponseEntity.ok(notificationService.getNotificationsByUserId(userId));
    }

    // ── GET /api/v1/notifications/unread?userId= ──────────────────────────────
    @Operation(summary = "Get unread notifications for a user")
    @PreAuthorize(ALL_ROLES)
    @GetMapping("/api/v1/notifications/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications(
            @RequestParam Long userId) {
        return ResponseEntity.ok(notificationService.getUnreadNotifications(userId));
    }

    // ── GET /api/v1/notifications/unread/count?userId= ────────────────────────
    @Operation(summary = "Get unread notification count for a user")
    @PreAuthorize(ALL_ROLES)
    @GetMapping("/api/v1/notifications/unread/count")
    public ResponseEntity<Long> getUnreadCount(@RequestParam Long userId) {
        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }

    // ── GET /api/v1/notifications/category?userId=&category= ─────────────────
    @Operation(summary = "Get notifications for a user filtered by category")
    @PreAuthorize(ALL_ROLES)
    @GetMapping("/api/v1/notifications/category")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByCategory(
            @RequestParam Long userId,
            @RequestParam NotificationCategory category) {
        return ResponseEntity.ok(
                notificationService.getNotificationsByCategory(userId, category));
    }

    // ── PATCH /api/v1/notifications/{id}/read ─────────────────────────────────
    @Operation(
        summary = "Mark notification as read — owner only",
        description = "Users can only mark their own notifications as read."
    )
    @ApiResponse(responseCode = "200", description = "Notification marked as read")
    @ApiResponse(responseCode = "400", description = "Not your notification")
    @PreAuthorize(ALL_ROLES)
    @PatchMapping("/api/v1/notifications/{notificationId}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long notificationId, Authentication authentication) {
        return ResponseEntity.ok(
                notificationService.markAsRead(notificationId, extractUserId(authentication)));
    }

    // ── PATCH /api/v1/notifications/{id}/dismiss ──────────────────────────────
    @Operation(
        summary = "Dismiss notification — owner only",
        description = "Users can only dismiss their own notifications."
    )
    @ApiResponse(responseCode = "200", description = "Notification dismissed")
    @ApiResponse(responseCode = "400", description = "Not your notification")
    @PreAuthorize(ALL_ROLES)
    @PatchMapping("/api/v1/notifications/{notificationId}/dismiss")
    public ResponseEntity<NotificationResponse> dismissNotification(
            @PathVariable Long notificationId, Authentication authentication) {
        return ResponseEntity.ok(
                notificationService.dismissNotification(
                        notificationId, extractUserId(authentication)));
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    private Long extractUserId(Authentication auth) {
        return ((Number) ((UsernamePasswordAuthenticationToken) auth).getDetails()).longValue();
    }
}
