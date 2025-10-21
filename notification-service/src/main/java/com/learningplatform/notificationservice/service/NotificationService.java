package com.learningplatform.notificationservice.service;

import com.learningplatform.notificationservice.dto.NotificationRequestDto;
import com.learningplatform.notificationservice.dto.NotificationResponseDto;
import com.learningplatform.notificationservice.entity.Notification;
import com.learningplatform.notificationservice.entity.NotificationCategory;
import com.learningplatform.notificationservice.repository.NotificationPreferenceRepository;
import com.learningplatform.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final EmailService emailService;

    /**
     * Create a new notification
     */
    @Transactional
    public NotificationResponseDto createNotification(NotificationRequestDto request) {
        // Check user preferences
        boolean inAppEnabled = preferenceRepository.isInAppEnabled(request.getUserId(), request.getCategory());
        boolean emailEnabled = preferenceRepository.isEmailEnabled(request.getUserId(), request.getCategory());

        Notification notification = null;

        // Create in-app notification if enabled
        if (inAppEnabled) {
            notification = Notification.builder()
                    .userId(request.getUserId())
                    .title(request.getTitle())
                    .message(request.getMessage())
                    .type(request.getType())
                    .category(request.getCategory())
                    .referenceId(request.getReferenceId())
                    .actionUrl(request.getActionUrl())
                    .emailSent(false)
                    .build();

            notification = notificationRepository.save(notification);
            log.info("In-app notification created for user {}: {}", request.getUserId(), request.getTitle());
        }

        // Send email if requested and enabled
        if (request.getSendEmail() && emailEnabled) {
            emailService.sendSimpleEmail(
                    getUserEmail(request.getUserId()),  // TODO: Fetch from User Service
                    request.getTitle(),
                    request.getMessage()
            );

            if (notification != null) {
                notification.setEmailSent(true);
                notification = notificationRepository.save(notification);
            }

            log.info("Email notification sent to user {}", request.getUserId());
        }

        return notification != null ? mapToResponseDto(notification) : null;
    }

    /**
     * Get all notifications for a user
     */
    public List<NotificationResponseDto> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Get unread notifications for a user
     */
    public List<NotificationResponseDto> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Get notifications by category
     */
    public List<NotificationResponseDto> getNotificationsByCategory(Long userId, NotificationCategory category) {
        return notificationRepository.findByUserIdAndCategoryOrderByCreatedAtDesc(userId, category)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Mark notification as read
     */
    @Transactional
    public NotificationResponseDto markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setIsRead(true);
        Notification updated = notificationRepository.save(notification);

        log.info("Notification {} marked as read", notificationId);
        return mapToResponseDto(updated);
    }

    /**
     * Mark all notifications as read for a user
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unreadNotifications = notificationRepository
                .findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false);

        unreadNotifications.forEach(notification -> notification.setIsRead(true));
        notificationRepository.saveAll(unreadNotifications);

        log.info("All notifications marked as read for user {}", userId);
    }

    /**
     * Get unread count for a user
     */
    public Long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }

    /**
     * Delete notification
     */
    @Transactional
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
        log.info("Notification {} deleted", notificationId);
    }

    /**
     * Delete all read notifications for a user
     */
    @Transactional
    public void deleteReadNotifications(Long userId) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        notificationRepository.deleteOldReadNotifications(userId, thirtyDaysAgo);
        log.info("Old read notifications deleted for user {}", userId);
    }

    /**
     * Get recent notifications (last 7 days)
     */
    public List<NotificationResponseDto> getRecentNotifications(Long userId, Integer days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days != null ? days : 7);
        return notificationRepository.findRecentNotifications(userId, since)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Map entity to DTO
     */
    private NotificationResponseDto mapToResponseDto(Notification notification) {
        return NotificationResponseDto.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .category(notification.getCategory())
                .isRead(notification.getIsRead())
                .emailSent(notification.getEmailSent())
                .referenceId(notification.getReferenceId())
                .actionUrl(notification.getActionUrl())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }

    /**
     * TODO: Fetch user email from User Service via REST call
     */
    private String getUserEmail(Long userId) {
        // Placeholder - should call User Service
        return "user" + userId + "@example.com";
    }
}
