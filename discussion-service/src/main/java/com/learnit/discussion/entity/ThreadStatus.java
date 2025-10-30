package com.learnit.discussion.entity;

public enum ThreadStatus {
    ACTIVE,     // Normal, active thread
    LOCKED,     // No new comments allowed
    ARCHIVED,   // Old/inactive thread
    DELETED     // Soft deleted
}
