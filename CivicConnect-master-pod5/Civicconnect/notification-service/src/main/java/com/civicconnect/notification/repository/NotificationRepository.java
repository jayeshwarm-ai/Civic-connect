package com.civicconnect.notification.repository;

import com.civicconnect.notification.entity.Notification;
import com.civicconnect.notification.enums.NotificationCategory;
import com.civicconnect.notification.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedDateDesc(Long userId);

    List<Notification> findByUserIdAndStatusOrderByCreatedDateDesc(Long userId, NotificationStatus status);

    List<Notification> findByUserIdAndCategoryOrderByCreatedDateDesc(Long userId, NotificationCategory category);

    long countByUserIdAndStatus(Long userId, NotificationStatus status);
}
