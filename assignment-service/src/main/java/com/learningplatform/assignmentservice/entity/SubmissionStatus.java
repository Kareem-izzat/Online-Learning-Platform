package com.learningplatform.assignmentservice.entity;

/**
 * Status of a submission throughout its lifecycle
 */
public enum SubmissionStatus {
    NOT_SUBMITTED,    // Assignment created but student hasn't submitted yet
    SUBMITTED,        // Student submitted, awaiting grading
    GRADED,           // Instructor/system has graded the submission
    LATE,             // Submitted after due date
    RESUBMITTED       // Student resubmitted after initial grading
}
