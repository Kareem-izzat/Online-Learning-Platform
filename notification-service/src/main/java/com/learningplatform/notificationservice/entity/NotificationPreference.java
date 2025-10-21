package com.learningplatform.notificationservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_preferences", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "category"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationCategory category;

    @Builder.Default
    @Column(nullable = false)
    private Boolean emailEnabled = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean inAppEnabled = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean smsEnabled = false;  // For future SMS integration

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (emailEnabled == null) {
            emailEnabled = true;
        }
        if (inAppEnabled == null) {
            inAppEnabled = true;
        }
        if (smsEnabled == null) {
            smsEnabled = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
