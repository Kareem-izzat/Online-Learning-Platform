package com.learningplatform.notificationservice.entity;

/**
 * Enum for notification types
 */
public enum NotificationType {
    INFO,       // General information
    SUCCESS,    // Success messages (enrollment confirmed, assignment submitted)
    WARNING,    // Warning messages (assignment deadline approaching)
    ERROR       // Error messages (payment failed, submission error)
}
