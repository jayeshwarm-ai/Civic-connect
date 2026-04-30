package com.civicconnect.notification;

import com.civicconnect.notification.dto.request.SendNotificationRequest;
import com.civicconnect.notification.dto.response.NotificationResponse;
import com.civicconnect.notification.entity.Notification;
import com.civicconnect.notification.enums.NotificationCategory;
import com.civicconnect.notification.enums.NotificationStatus;
import com.civicconnect.notification.exception.InvalidOperationException;
import com.civicconnect.notification.exception.ResourceNotFoundException;
import com.civicconnect.notification.feign.IdentityFeignClient;
import com.civicconnect.notification.feign.dto.UserValidationResponse;
import com.civicconnect.notification.repository.NotificationRepository;
import com.civicconnect.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService Tests")
class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private IdentityFeignClient    identityFeignClient;

    @InjectMocks
    private NotificationService notificationService;

    private Notification unreadNotification;
    private UserValidationResponse validUser;

    @BeforeEach
    void setUp() {
        validUser = UserValidationResponse.builder()
                .userId(100L).name("Alice").role("CITIZEN").exists(true).build();

        unreadNotification = Notification.builder()
                .notificationId(1L).userId(100L).requestId(20L)
                .message("Your request #20 has been submitted.")
                .category(NotificationCategory.REQUEST)
                .status(NotificationStatus.UNREAD)
                .createdDate(LocalDateTime.now()).build();
    }

    // ── sendNotification (internal) ────────────────────────────────────────────

    @Test
    @DisplayName("Should send notification successfully with valid category string")
    void shouldSendNotificationSuccessfully() {
        SendNotificationRequest req = new SendNotificationRequest();
        req.setUserId(100L);
        req.setRequestId(20L);
        req.setMessage("Your request has been submitted.");
        req.setCategory("REQUEST");

        when(notificationRepository.save(any())).thenReturn(unreadNotification);

        notificationService.sendNotification(req);

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should default to REQUEST category when unknown category string received")
    void shouldDefaultToRequestForUnknownCategory() {
        SendNotificationRequest req = new SendNotificationRequest();
        req.setUserId(100L);
        req.setMessage("Test notification");
        req.setCategory("UNKNOWN_CATEGORY");

        when(notificationRepository.save(any())).thenReturn(unreadNotification);

        // Should not throw — defaults to REQUEST
        assertThatNoException().isThrownBy(() -> notificationService.sendNotification(req));
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should send notification with null requestId (non-request notifications)")
    void shouldSendNotificationWithNullRequestId() {
        SendNotificationRequest req = new SendNotificationRequest();
        req.setUserId(100L);
        req.setRequestId(null);
        req.setMessage("Report generated.");
        req.setCategory("REPORT");

        Notification reportNotif = Notification.builder()
                .notificationId(2L).userId(100L).requestId(null)
                .message("Report generated.").category(NotificationCategory.REPORT)
                .status(NotificationStatus.UNREAD).createdDate(LocalDateTime.now()).build();

        when(notificationRepository.save(any())).thenReturn(reportNotif);

        notificationService.sendNotification(req);

        verify(notificationRepository).save(argThat(n -> n.getRequestId() == null));
    }

    // ── getNotificationsByUserId ───────────────────────────────────────────────

    @Test
    @DisplayName("Should return all notifications for a user")
    void shouldReturnAllNotificationsForUser() {
        when(identityFeignClient.validateUser(100L)).thenReturn(validUser);
        when(notificationRepository.findByUserIdOrderByCreatedDateDesc(100L))
                .thenReturn(List.of(unreadNotification));

        List<NotificationResponse> results =
                notificationService.getNotificationsByUserId(100L);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getMessage()).isEqualTo("Your request #20 has been submitted.");
        assertThat(results.get(0).getStatus()).isEqualTo(NotificationStatus.UNREAD);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found")
    void shouldThrowWhenUserNotFound() {
        when(identityFeignClient.validateUser(999L))
                .thenReturn(UserValidationResponse.builder().userId(999L).exists(false).build());

        assertThatThrownBy(() -> notificationService.getNotificationsByUserId(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    // ── getUnreadNotifications ─────────────────────────────────────────────────

    @Test
    @DisplayName("Should return only unread notifications")
    void shouldReturnUnreadNotifications() {
        when(identityFeignClient.validateUser(100L)).thenReturn(validUser);
        when(notificationRepository.findByUserIdAndStatusOrderByCreatedDateDesc(
                100L, NotificationStatus.UNREAD))
                .thenReturn(List.of(unreadNotification));

        List<NotificationResponse> results =
                notificationService.getUnreadNotifications(100L);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getStatus()).isEqualTo(NotificationStatus.UNREAD);
    }

    // ── getUnreadCount ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return correct unread count")
    void shouldReturnUnreadCount() {
        when(notificationRepository.countByUserIdAndStatus(100L, NotificationStatus.UNREAD))
                .thenReturn(3L);

        long count = notificationService.getUnreadCount(100L);

        assertThat(count).isEqualTo(3L);
    }

    // ── markAsRead ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should mark notification as READ successfully")
    void shouldMarkNotificationAsRead() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(unreadNotification));
        when(notificationRepository.save(any())).thenReturn(unreadNotification);

        NotificationResponse response = notificationService.markAsRead(1L, 100L);

        assertThat(response).isNotNull();
        verify(notificationRepository).save(argThat(
                n -> n.getStatus() == NotificationStatus.READ));
    }

    @Test
    @DisplayName("Should throw InvalidOperationException when wrong user tries to mark as read")
    void shouldThrowWhenWrongUserMarksAsRead() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(unreadNotification));

        // userId 999 is not the owner (100 is)
        assertThatThrownBy(() -> notificationService.markAsRead(1L, 999L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("your own notifications");
    }

    // ── dismissNotification ────────────────────────────────────────────────────

    @Test
    @DisplayName("Should dismiss notification successfully")
    void shouldDismissNotification() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(unreadNotification));
        when(notificationRepository.save(any())).thenReturn(unreadNotification);

        NotificationResponse response = notificationService.dismissNotification(1L, 100L);

        assertThat(response).isNotNull();
        verify(notificationRepository).save(argThat(
                n -> n.getStatus() == NotificationStatus.DISMISSED));
    }

    @Test
    @DisplayName("Should throw InvalidOperationException when wrong user tries to dismiss")
    void shouldThrowWhenWrongUserDismisses() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(unreadNotification));

        assertThatThrownBy(() -> notificationService.dismissNotification(1L, 999L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("your own notifications");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when notification not found")
    void shouldThrowWhenNotificationNotFound() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(99L, 100L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Notification not found");
    }

    // ── getNotificationsByCategory ─────────────────────────────────────────────

    @Test
    @DisplayName("Should return notifications filtered by category")
    void shouldReturnNotificationsByCategory() {
        when(identityFeignClient.validateUser(100L)).thenReturn(validUser);
        when(notificationRepository.findByUserIdAndCategoryOrderByCreatedDateDesc(
                100L, NotificationCategory.REQUEST))
                .thenReturn(List.of(unreadNotification));

        List<NotificationResponse> results =
                notificationService.getNotificationsByCategory(100L, NotificationCategory.REQUEST);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getCategory()).isEqualTo(NotificationCategory.REQUEST);
    }
}
