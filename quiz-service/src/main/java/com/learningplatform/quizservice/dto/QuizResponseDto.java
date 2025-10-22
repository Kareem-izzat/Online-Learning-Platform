package com.learningplatform.quizservice.dto;

import com.learningplatform.quizservice.entity.QuizStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizResponseDto {

    private Long id;
    private Long courseId;
    private Long instructorId;
    private String title;
    private String description;
    private String instructions;
    private QuizStatus status;
    private Integer timeLimitMinutes;
    private Integer maxAttempts;
    private Double passingScore;
    private Boolean randomizeQuestions;
    private Boolean showCorrectAnswers;
    private Boolean allowReview;
    private LocalDateTime availableFrom;
    private LocalDateTime availableUntil;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer questionCount;  // Calculated field
    private Double totalPoints;     // Calculated field
}
