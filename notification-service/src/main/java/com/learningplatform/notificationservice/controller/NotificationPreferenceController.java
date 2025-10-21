package com.learningplatform.notificationservice.controller;

import com.learningplatform.notificationservice.dto.NotificationPreferenceDto;
import com.learningplatform.notificationservice.entity.NotificationCategory;
import com.learningplatform.notificationservice.service.NotificationPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notification-preferences")
@RequiredArgsConstructor
public class NotificationPreferenceController {

    private final NotificationPreferenceService preferenceService;

    /**
     * Get all preferences for a user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationPreferenceDto>> getUserPreferences(
            @PathVariable Long userId) {
        List<NotificationPreferenceDto> preferences = preferenceService.getUserPreferences(userId);
        return ResponseEntity.ok(preferences);
    }

    /**
     * Get specific preference
     */
    @GetMapping("/user/{userId}/category/{category}")
    public ResponseEntity<NotificationPreferenceDto> getPreference(
            @PathVariable Long userId,
            @PathVariable NotificationCategory category) {
        NotificationPreferenceDto preference = preferenceService.getPreference(userId, category);
        return ResponseEntity.ok(preference);
    }

    /**
     * Update notification preference
     */
    @PutMapping("/user/{userId}/category/{category}")
    public ResponseEntity<NotificationPreferenceDto> updatePreference(
            @PathVariable Long userId,
            @PathVariable NotificationCategory category,
            @RequestParam(required = false) Boolean emailEnabled,
            @RequestParam(required = false) Boolean inAppEnabled,
            @RequestParam(required = false) Boolean smsEnabled) {
        NotificationPreferenceDto preference = preferenceService.updatePreference(
                userId, category, emailEnabled, inAppEnabled, smsEnabled);
        return ResponseEntity.ok(preference);
    }

    /**
     * Initialize default preferences for a new user
     */
    @PostMapping("/user/{userId}/initialize")
    public ResponseEntity<List<NotificationPreferenceDto>> initializeDefaultPreferences(
            @PathVariable Long userId) {
        List<NotificationPreferenceDto> preferences = 
                preferenceService.initializeDefaultPreferences(userId);
        return ResponseEntity.ok(preferences);
    }

    /**
     * Disable all email notifications
     */
    @PutMapping("/user/{userId}/disable-all-email")
    public ResponseEntity<Void> disableAllEmailNotifications(@PathVariable Long userId) {
        preferenceService.disableAllEmailNotifications(userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Enable all email notifications
     */
    @PutMapping("/user/{userId}/enable-all-email")
    public ResponseEntity<Void> enableAllEmailNotifications(@PathVariable Long userId) {
        preferenceService.enableAllEmailNotifications(userId);
        return ResponseEntity.ok().build();
    }
}
