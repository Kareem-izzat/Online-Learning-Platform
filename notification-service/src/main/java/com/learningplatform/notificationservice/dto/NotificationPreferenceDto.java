package com.learningplatform.notificationservice.dto;

import com.learningplatform.notificationservice.entity.NotificationCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreferenceDto {

    private Long id;
    private Long userId;
    private NotificationCategory category;
    private Boolean emailEnabled;
    private Boolean inAppEnabled;
    private Boolean smsEnabled;
}
