package com.learningplatform.enrollmentservice.entity;

public enum EnrollmentStatus {
    ACTIVE,      // Student is currently enrolled and learning
    COMPLETED,   // Student has completed the course
    CANCELLED,   // Enrollment was cancelled
    EXPIRED      // Enrollment access has expired
}
