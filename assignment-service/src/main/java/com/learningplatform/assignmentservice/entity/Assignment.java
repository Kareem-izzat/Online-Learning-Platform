package com.learningplatform.assignmentservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Assignment entity - represents homework, quizzes, and projects assigned to students
 */
@Entity
@Table(name = "assignments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long courseId;  // Foreign key to Course in course-service

    private Long lessonId;  // Optional: Link to specific lesson

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(length = 5000)
    private String instructions;  // Detailed instructions for students

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentType type;

    @Column(nullable = false)
    private Integer totalPoints;  // Maximum points possible

    private Integer passingScore;  // Minimum points to pass

    private LocalDateTime dueDate;  // When assignment is due

    private Boolean allowLateSubmission;  // Can students submit after due date?

    private Integer latePenaltyPercent;  // Percentage deduction for late submissions

    private Integer maxAttempts;  // How many times student can submit (null = unlimited)

    private Integer timeLimitMinutes;  // Time limit in minutes (for timed quizzes)

    @Column(nullable = false)
    private Long createdBy;  // Instructor who created the assignment

    @Column(nullable = false)
    private Boolean published;  // Is assignment visible to students?

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime publishedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (published == null) {
            published = false;
        }
        if (allowLateSubmission == null) {
            allowLateSubmission = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (published && publishedAt == null) {
            publishedAt = LocalDateTime.now();
        }
    }
}
