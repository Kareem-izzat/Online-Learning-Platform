package com.learningplatform.quizservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_attempts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long quizId;

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false)
    @Builder.Default
    private Integer attemptNumber = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AttemptStatus status = AttemptStatus.IN_PROGRESS;

    private LocalDateTime startedAt;

    private LocalDateTime submittedAt;

    private LocalDateTime completedAt;  // When grading finished

    @Column(nullable = false)
    @Builder.Default
    private Double score = 0.0;  // Percentage (0-100)

    @Column(nullable = false)
    @Builder.Default
    private Double earnedPoints = 0.0;

    @Column(nullable = false)
    @Builder.Default
    private Double totalPoints = 0.0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean passed = false;

    private Integer timeSpentMinutes;

    @Column(length = 1000)
    private String feedback;  // Instructor feedback

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        startedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
