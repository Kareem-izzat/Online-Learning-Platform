package com.learningplatform.quizservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "quizzes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long courseId;

    @Column(nullable = false)
    private Long instructorId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(length = 1000)
    private String instructions;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private QuizStatus status = QuizStatus.DRAFT;

    private Integer timeLimitMinutes;  // null = no time limit

    private Integer maxAttempts;  // null = unlimited attempts

    @Column(nullable = false)
    @Builder.Default
    private Double passingScore = 70.0;  // Percentage needed to pass

    @Column(nullable = false)
    @Builder.Default
    private Boolean randomizeQuestions = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean showCorrectAnswers = true;  // After submission

    @Column(nullable = false)
    @Builder.Default
    private Boolean allowReview = true;  // Can review after submission

    private LocalDateTime availableFrom;  // null = immediately available

    private LocalDateTime availableUntil;  // null = no deadline

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
