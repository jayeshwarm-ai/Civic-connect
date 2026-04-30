package com.civicconnect.notification.dto.response;

import com.civicconnect.notification.enums.NotificationCategory;
import com.civicconnect.notification.enums.NotificationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationResponse {

    private Long                 notificationId;
    private Long                 userId;
    private Long                 requestId;
    private String               message;
    private NotificationCategory category;
    private NotificationStatus   status;
    private LocalDateTime        createdDate;
}
