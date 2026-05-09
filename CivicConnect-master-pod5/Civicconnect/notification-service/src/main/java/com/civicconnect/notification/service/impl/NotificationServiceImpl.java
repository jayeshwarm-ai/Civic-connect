package com.civicconnect.notification.service.impl;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final IdentityFeignClient    identityFeignClient;

    @Override
    @Transactional
    public void sendNotification(SendNotificationRequest request) {
        NotificationCategory category;
        try {
            category = NotificationCategory.valueOf(request.getCategory());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown NotificationCategory '{}' — defaulting to REQUEST", request.getCategory());
            category = NotificationCategory.REQUEST;
        }

        Notification notification = Notification.builder()
                .userId(request.getUserId()).requestId(request.getRequestId())
                .message(request.getMessage()).category(category).build();
        notificationRepository.save(notification);
        log.debug("Notification saved for userId={}, category={}", request.getUserId(), category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsByUserId(Long userId) {
        validateUserExists(userId);
        return notificationRepository.findByUserIdOrderByCreatedDateDesc(userId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(Long userId) {
        validateUserExists(userId);
        return notificationRepository.findByUserIdAndStatusOrderByCreatedDateDesc(userId, NotificationStatus.UNREAD)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndStatus(userId, NotificationStatus.UNREAD);
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long notificationId, Long userId) {
        Notification notification = findById(notificationId);
        if (!notification.getUserId().equals(userId))
            throw new InvalidOperationException("You can only update your own notifications.");
        notification.setStatus(NotificationStatus.READ);
        notificationRepository.save(notification);
        return mapToResponse(notification);
    }

    @Override
    @Transactional
    public NotificationResponse dismissNotification(Long notificationId, Long userId) {
        Notification notification = findById(notificationId);
        if (!notification.getUserId().equals(userId))
            throw new InvalidOperationException("You can only dismiss your own notifications.");
        notification.setStatus(NotificationStatus.DISMISSED);
        notificationRepository.save(notification);
        return mapToResponse(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsByCategory(Long userId, NotificationCategory category) {
        validateUserExists(userId);
        return notificationRepository.findByUserIdAndCategoryOrderByCreatedDateDesc(userId, category)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private void validateUserExists(Long userId) {
        UserValidationResponse user = identityFeignClient.validateUser(userId);
        if (!user.isExists()) throw new ResourceNotFoundException("User", userId);
    }

    private Notification findById(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));
    }

    private NotificationResponse mapToResponse(Notification n) {
        return NotificationResponse.builder()
                .notificationId(n.getNotificationId()).userId(n.getUserId()).requestId(n.getRequestId())
                .message(n.getMessage()).category(n.getCategory()).status(n.getStatus())
                .createdDate(n.getCreatedDate()).build();
    }
}

