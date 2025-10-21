package com.learningplatform.notificationservice.dto;

import com.learningplatform.notificationservice.entity.NotificationCategory;
import com.learningplatform.notificationservice.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponseDto {

    private Long id;
    private Long userId;
    private String title;
    private String message;
    private NotificationType type;
    private NotificationCategory category;
    private Boolean isRead;
    private Boolean emailSent;
    private String referenceId;
    private String actionUrl;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
