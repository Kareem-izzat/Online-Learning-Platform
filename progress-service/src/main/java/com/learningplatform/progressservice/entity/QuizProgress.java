package com.learningplatform.progressservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_progress")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private Long courseId;

    @Column(nullable = false)
    private Long quizId;

    @Column(nullable = false)
    private Long attemptId;

    @Column(nullable = false)
    private Boolean completed = false;

    @Column(nullable = false)
    private Boolean passed = false;

    @Column(precision = 5, scale = 2)
    private BigDecimal score;

    private Integer attemptNumber = 1;

    @Column(precision = 5, scale = 2)
    private BigDecimal bestScore;

    private LocalDateTime completedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
