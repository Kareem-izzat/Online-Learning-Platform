package com.learningplatform.notificationservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;  // Recipient user ID

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;  // INFO, SUCCESS, WARNING, ERROR

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationCategory category;  // USER, COURSE, ENROLLMENT, ASSIGNMENT, etc.

    @Builder.Default
    @Column(nullable = false)
    private Boolean isRead = false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean emailSent = false;

    private String referenceId;  // ID of related entity (courseId, assignmentId, etc.)

    private String actionUrl;  // Deep link to related page

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime readAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isRead == null) {
            isRead = false;
        }
        if (emailSent == null) {
            emailSent = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (isRead && readAt == null) {
            readAt = LocalDateTime.now();
        }
    }
}
