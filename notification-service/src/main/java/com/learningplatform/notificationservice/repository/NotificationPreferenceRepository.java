package com.learningplatform.notificationservice.repository;

import com.learningplatform.notificationservice.entity.NotificationCategory;
import com.learningplatform.notificationservice.entity.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {

    // Find all preferences for a user
    List<NotificationPreference> findByUserId(Long userId);

    // Find specific preference for user and category
    Optional<NotificationPreference> findByUserIdAndCategory(Long userId, NotificationCategory category);

    // Check if user has email enabled for a category
    default boolean isEmailEnabled(Long userId, NotificationCategory category) {
        return findByUserIdAndCategory(userId, category)
                .map(NotificationPreference::getEmailEnabled)
                .orElse(true);  // Default: enabled
    }

    // Check if user has in-app enabled for a category
    default boolean isInAppEnabled(Long userId, NotificationCategory category) {
        return findByUserIdAndCategory(userId, category)
                .map(NotificationPreference::getInAppEnabled)
                .orElse(true);  // Default: enabled
    }
}
