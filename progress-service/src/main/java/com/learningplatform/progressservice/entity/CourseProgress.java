package com.learningplatform.progressservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "course_progress")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private Long courseId;

    @Column(nullable = false)
    private Long enrollmentId;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal completionPercentage = BigDecimal.ZERO;

    private Integer totalLessons = 0;
    private Integer completedLessons = 0;

    private Integer totalQuizzes = 0;
    private Integer completedQuizzes = 0;

    private Integer totalAssignments = 0;
    private Integer completedAssignments = 0;

    @Column(precision = 5, scale = 2)
    private BigDecimal averageQuizScore;

    @Column(precision = 5, scale = 2)
    private BigDecimal averageAssignmentScore;

    private LocalDateTime lastActivityAt;

    private LocalDateTime completedAt;

    private Boolean certificateIssued = false;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        lastActivityAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        lastActivityAt = LocalDateTime.now();
    }
}
