package com.learningplatform.assignmentservice.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Submission entity - represents a student's submission for an assignment
 */
@Entity
@Table(name = "submissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long assignmentId;  // Foreign key to Assignment

    @Column(nullable = false)
    private Long studentId;  // Foreign key to User in user-service

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SubmissionStatus status = SubmissionStatus.NOT_SUBMITTED;

    @Column(length = 5000)
    private String submissionText;  // For essay/text submissions

    private String fileUrl;  // Path to uploaded file (if any)

    private String fileName;  // Original file name

    private Long fileSize;  // File size in bytes

    private Integer score;  // Points earned (null if not graded yet)

    @Column(length = 2000)
    private String feedback;  // Instructor feedback

    private Integer attemptNumber;  // Which attempt is this (1, 2, 3...)

    private LocalDateTime submittedAt;  // When student submitted

    private LocalDateTime gradedAt;  // When instructor graded

    private Long gradedBy;  // Instructor who graded (null for auto-graded)

    private Boolean isLate;  // Was this submitted after due date?

    private Integer timeTakenMinutes;  // How long student took (for timed assignments)

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (attemptNumber == null) {
            attemptNumber = 1;
        }
        if (isLate == null) {
            isLate = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (status == SubmissionStatus.SUBMITTED && submittedAt == null) {
            submittedAt = LocalDateTime.now();
        }
        if (status == SubmissionStatus.GRADED && gradedAt == null) {
            gradedAt = LocalDateTime.now();
        }
    }
}
