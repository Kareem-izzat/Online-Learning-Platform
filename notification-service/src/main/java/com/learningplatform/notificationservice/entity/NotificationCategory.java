package com.learningplatform.notificationservice.entity;

/**
 * Enum for notification categories
 */
public enum NotificationCategory {
    USER,           // User-related (registration, profile updates)
    COURSE,         // Course-related (new course, updates)
    ENROLLMENT,     // Enrollment-related (enrollment confirmed, course started)
    ASSIGNMENT,     // Assignment-related (new assignment, deadline, grading)
    VIDEO,          // Video-related (new video uploaded)
    PAYMENT,        // Payment-related (payment successful, refund)
    SYSTEM          // System notifications (maintenance, updates)
}
