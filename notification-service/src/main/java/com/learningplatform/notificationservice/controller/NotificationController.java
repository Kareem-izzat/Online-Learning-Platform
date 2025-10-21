package com.learningplatform.notificationservice.controller;

import com.learningplatform.notificationservice.dto.NotificationRequestDto;
import com.learningplatform.notificationservice.dto.NotificationResponseDto;
import com.learningplatform.notificationservice.entity.NotificationCategory;
import com.learningplatform.notificationservice.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Create a new notification
     */
    @PostMapping
    public ResponseEntity<NotificationResponseDto> createNotification(
            @Valid @RequestBody NotificationRequestDto request) {
        NotificationResponseDto notification = notificationService.createNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(notification);
    }

    /**
     * Get all notifications for a user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponseDto>> getUserNotifications(
            @PathVariable Long userId) {
        List<NotificationResponseDto> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get unread notifications for a user
     */
    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<NotificationResponseDto>> getUnreadNotifications(
            @PathVariable Long userId) {
        List<NotificationResponseDto> notifications = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get unread count for a user
     */
    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Long> getUnreadCount(@PathVariable Long userId) {
        Long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(count);
    }

    /**
     * Get notifications by category
     */
    @GetMapping("/user/{userId}/category/{category}")
    public ResponseEntity<List<NotificationResponseDto>> getNotificationsByCategory(
            @PathVariable Long userId,
            @PathVariable NotificationCategory category) {
        List<NotificationResponseDto> notifications = 
                notificationService.getNotificationsByCategory(userId, category);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get recent notifications (last N days)
     */
    @GetMapping("/user/{userId}/recent")
    public ResponseEntity<List<NotificationResponseDto>> getRecentNotifications(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "7") Integer days) {
        List<NotificationResponseDto> notifications = 
                notificationService.getRecentNotifications(userId, days);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Mark notification as read
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponseDto> markAsRead(@PathVariable Long id) {
        NotificationResponseDto notification = notificationService.markAsRead(id);
        return ResponseEntity.ok(notification);
    }

    /**
     * Mark all notifications as read for a user
     */
    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<Void> markAllAsRead(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Delete a notification
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Delete all read notifications for a user
     */
    @DeleteMapping("/user/{userId}/read")
    public ResponseEntity<Void> deleteReadNotifications(@PathVariable Long userId) {
        notificationService.deleteReadNotifications(userId);
        return ResponseEntity.noContent().build();
    }
}
