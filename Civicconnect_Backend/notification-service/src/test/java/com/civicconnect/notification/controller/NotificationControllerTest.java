package com.civicconnect.notification.controller;

import com.civicconnect.notification.dto.request.SendNotificationRequest;
import com.civicconnect.notification.dto.response.NotificationResponse;
import com.civicconnect.notification.enums.NotificationCategory;
import com.civicconnect.notification.enums.NotificationStatus;
import com.civicconnect.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationController Tests")
class NotificationControllerTest {

    @Mock private NotificationService notificationService;

    @InjectMocks
    private NotificationController controller;

    private UsernamePasswordAuthenticationToken authWithUserId(Long userId) {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("user", null, Collections.emptyList());
        auth.setDetails(userId);
        return auth;
    }

    private NotificationResponse sampleNotification;

    @BeforeEach
    void setUp() {
        sampleNotification = NotificationResponse.builder()
                .notificationId(1L)
                .userId(100L)
                .requestId(10L)
                .resolutionId(5L)
                .message("Your request has been resolved")
                .category(NotificationCategory.RESOLUTION)
                .status(NotificationStatus.UNREAD)
                .build();
    }

    @Test
    @DisplayName("sendNotification (internal) → 200 and delegates to service")
    void sendNotification_returns200() {
        SendNotificationRequest req = new SendNotificationRequest();
        req.setUserId(100L);
        req.setMessage("Hello");
        req.setCategory("RESOLUTION");

        ResponseEntity<Void> resp = controller.sendNotification(req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(notificationService).sendNotification(req);
    }

    @Test
    @DisplayName("getNotifications → 200 with the user's notifications")
    void getNotifications_returns200() {
        when(notificationService.getNotificationsByUserId(100L))
                .thenReturn(List.of(sampleNotification));

        ResponseEntity<List<NotificationResponse>> resp = controller.getNotifications(100L);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSize(1);
        assertThat(resp.getBody().get(0).getResolutionId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("getUnreadNotifications → 200")
    void getUnreadNotifications_returns200() {
        when(notificationService.getUnreadNotifications(100L))
                .thenReturn(List.of(sampleNotification));

        ResponseEntity<List<NotificationResponse>> resp = controller.getUnreadNotifications(100L);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("getUnreadCount → 200 with count")
    void getUnreadCount_returns200() {
        when(notificationService.getUnreadCount(100L)).thenReturn(7L);

        ResponseEntity<Long> resp = controller.getUnreadCount(100L);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isEqualTo(7L);
    }

    @Test
    @DisplayName("getNotificationsByCategory → 200 filtered")
    void getNotificationsByCategory_returns200() {
        when(notificationService.getNotificationsByCategory(100L, NotificationCategory.RESOLUTION))
                .thenReturn(List.of(sampleNotification));

        ResponseEntity<List<NotificationResponse>> resp =
                controller.getNotificationsByCategory(100L, NotificationCategory.RESOLUTION);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("markAsRead → 200, delegates with extracted userId")
    void markAsRead_returns200() {
        when(notificationService.markAsRead(eq(1L), eq(100L))).thenReturn(sampleNotification);

        ResponseEntity<NotificationResponse> resp =
                controller.markAsRead(1L, authWithUserId(100L));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(notificationService).markAsRead(eq(1L), eq(100L));
    }

    @Test
    @DisplayName("dismissNotification → 200, delegates with extracted userId")
    void dismissNotification_returns200() {
        when(notificationService.dismissNotification(eq(1L), eq(100L))).thenReturn(sampleNotification);

        ResponseEntity<NotificationResponse> resp =
                controller.dismissNotification(1L, authWithUserId(100L));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(notificationService).dismissNotification(eq(1L), eq(100L));
    }
}
