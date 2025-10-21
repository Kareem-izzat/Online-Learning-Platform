package com.learningplatform.notificationservice.dto;

import com.learningplatform.notificationservice.entity.NotificationCategory;
import com.learningplatform.notificationservice.entity.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequestDto {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Message is required")
    private String message;

    @NotNull(message = "Type is required")
    private NotificationType type;

    @NotNull(message = "Category is required")
    private NotificationCategory category;

    private String referenceId;  // Optional: ID of related entity

    private String actionUrl;    // Optional: Deep link URL

    @Builder.Default
    private Boolean sendEmail = false;  // Should send email notification?
}
