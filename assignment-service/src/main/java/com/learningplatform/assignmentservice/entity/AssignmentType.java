package com.learningplatform.assignmentservice.entity;

/**
 * Assignment types supported by the platform
 */
public enum AssignmentType {
    MULTIPLE_CHOICE,  // Quiz with multiple choice questions
    ESSAY,            // Written essay/text submission
    FILE_UPLOAD,      // Submit files (code, documents, etc.)
    CODING,           // Programming assignment with auto-grading
    MIXED             // Combination of different types
}
