package com.learningplatform.notificationservice.repository;

import com.learningplatform.notificationservice.entity.Notification;
import com.learningplatform.notificationservice.entity.NotificationCategory;
import com.learningplatform.notificationservice.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Find all notifications for a user
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Find unread notifications for a user
    List<Notification> findByUserIdAndIsReadOrderByCreatedAtDesc(Long userId, Boolean isRead);

    // Find notifications by category
    List<Notification> findByUserIdAndCategoryOrderByCreatedAtDesc(Long userId, NotificationCategory category);

    // Find notifications by type
    List<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(Long userId, NotificationType type);

    // Count unread notifications for a user
    Long countByUserIdAndIsRead(Long userId, Boolean isRead);

    // Find recent notifications (last N days)
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.createdAt > :since ORDER BY n.createdAt DESC")
    List<Notification> findRecentNotifications(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    // Find notifications by reference ID
    List<Notification> findByReferenceId(String referenceId);

    // Delete old read notifications
    @Query("DELETE FROM Notification n WHERE n.userId = :userId AND n.isRead = true AND n.createdAt < :before")
    void deleteOldReadNotifications(@Param("userId") Long userId, @Param("before") LocalDateTime before);
}
