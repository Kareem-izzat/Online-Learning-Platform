package com.learningplatform.quizservice.entity;

public enum AttemptStatus {
    IN_PROGRESS,  // Student currently taking quiz
    SUBMITTED,    // Submitted, awaiting grading
    GRADED,       // Fully graded
    EXPIRED       // Time limit exceeded
}
