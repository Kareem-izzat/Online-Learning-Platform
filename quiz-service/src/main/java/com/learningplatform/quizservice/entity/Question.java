package com.learningplatform.quizservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long quizId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType type;

    @Column(nullable = false, length = 1000)
    private String questionText;

    @Column(length = 500)
    private String imageUrl;  // Optional image for question

    @Column(columnDefinition = "TEXT")
    private String options;  // JSON array: ["Option A", "Option B", "Option C", "Option D"]

    @Column(columnDefinition = "TEXT")
    private String correctAnswers;  // JSON array: ["Option A"] or ["Option A", "Option C"] for multiple select

    @Column(length = 1000)
    private String explanation;  // Shown after answering

    @Column(nullable = false)
    @Builder.Default
    private Double points = 1.0;

    @Column(nullable = false)
    @Builder.Default
    private Integer orderIndex = 0;  // Question order in quiz

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
