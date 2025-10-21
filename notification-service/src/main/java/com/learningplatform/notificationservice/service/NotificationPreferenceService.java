package com.learningplatform.notificationservice.service;

import com.learningplatform.notificationservice.dto.NotificationPreferenceDto;
import com.learningplatform.notificationservice.entity.NotificationCategory;
import com.learningplatform.notificationservice.entity.NotificationPreference;
import com.learningplatform.notificationservice.repository.NotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPreferenceService {

    private final NotificationPreferenceRepository preferenceRepository;

    /**
     * Get all preferences for a user
     */
    public List<NotificationPreferenceDto> getUserPreferences(Long userId) {
        return preferenceRepository.findByUserId(userId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get specific preference for user and category
     */
    public NotificationPreferenceDto getPreference(Long userId, NotificationCategory category) {
        return preferenceRepository.findByUserIdAndCategory(userId, category)
                .map(this::mapToDto)
                .orElse(createDefaultPreference(userId, category));
    }

    /**
     * Update notification preference
     */
    @Transactional
    public NotificationPreferenceDto updatePreference(Long userId, NotificationCategory category, 
                                                      Boolean emailEnabled, Boolean inAppEnabled, Boolean smsEnabled) {
        NotificationPreference preference = preferenceRepository
                .findByUserIdAndCategory(userId, category)
                .orElse(NotificationPreference.builder()
                        .userId(userId)
                        .category(category)
                        .build());

        if (emailEnabled != null) {
            preference.setEmailEnabled(emailEnabled);
        }
        if (inAppEnabled != null) {
            preference.setInAppEnabled(inAppEnabled);
        }
        if (smsEnabled != null) {
            preference.setSmsEnabled(smsEnabled);
        }

        NotificationPreference saved = preferenceRepository.save(preference);
        log.info("Notification preference updated for user {} and category {}", userId, category);

        return mapToDto(saved);
    }

    /**
     * Initialize default preferences for a new user
     */
    @Transactional
    public List<NotificationPreferenceDto> initializeDefaultPreferences(Long userId) {
        List<NotificationPreference> preferences = List.of(
                createPreferenceEntity(userId, NotificationCategory.USER),
                createPreferenceEntity(userId, NotificationCategory.COURSE),
                createPreferenceEntity(userId, NotificationCategory.ENROLLMENT),
                createPreferenceEntity(userId, NotificationCategory.ASSIGNMENT),
                createPreferenceEntity(userId, NotificationCategory.VIDEO),
                createPreferenceEntity(userId, NotificationCategory.PAYMENT),
                createPreferenceEntity(userId, NotificationCategory.SYSTEM)
        );

        List<NotificationPreference> saved = preferenceRepository.saveAll(preferences);
        log.info("Default notification preferences initialized for user {}", userId);

        return saved.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Disable all email notifications for a user
     */
    @Transactional
    public void disableAllEmailNotifications(Long userId) {
        List<NotificationPreference> preferences = preferenceRepository.findByUserId(userId);
        preferences.forEach(pref -> pref.setEmailEnabled(false));
        preferenceRepository.saveAll(preferences);
        log.info("All email notifications disabled for user {}", userId);
    }

    /**
     * Enable all email notifications for a user
     */
    @Transactional
    public void enableAllEmailNotifications(Long userId) {
        List<NotificationPreference> preferences = preferenceRepository.findByUserId(userId);
        preferences.forEach(pref -> pref.setEmailEnabled(true));
        preferenceRepository.saveAll(preferences);
        log.info("All email notifications enabled for user {}", userId);
    }

    /**
     * Create default preference entity
     */
    private NotificationPreference createPreferenceEntity(Long userId, NotificationCategory category) {
        return NotificationPreference.builder()
                .userId(userId)
                .category(category)
                .emailEnabled(true)
                .inAppEnabled(true)
                .smsEnabled(false)
                .build();
    }

    /**
     * Create default preference DTO
     */
    private NotificationPreferenceDto createDefaultPreference(Long userId, NotificationCategory category) {
        return NotificationPreferenceDto.builder()
                .userId(userId)
                .category(category)
                .emailEnabled(true)
                .inAppEnabled(true)
                .smsEnabled(false)
                .build();
    }

    /**
     * Map entity to DTO
     */
    private NotificationPreferenceDto mapToDto(NotificationPreference preference) {
        return NotificationPreferenceDto.builder()
                .id(preference.getId())
                .userId(preference.getUserId())
                .category(preference.getCategory())
                .emailEnabled(preference.getEmailEnabled())
                .inAppEnabled(preference.getInAppEnabled())
                .smsEnabled(preference.getSmsEnabled())
                .build();
    }
}
