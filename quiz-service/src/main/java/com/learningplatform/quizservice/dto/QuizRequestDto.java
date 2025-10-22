package com.learningplatform.quizservice.dto;

import com.learningplatform.quizservice.entity.QuizStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizRequestDto {

    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotNull(message = "Instructor ID is required")
    private Long instructorId;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must be less than 200 characters")
    private String title;

    @Size(max = 2000, message = "Description must be less than 2000 characters")
    private String description;

    @Size(max = 1000, message = "Instructions must be less than 1000 characters")
    private String instructions;

    private QuizStatus status;

    @Min(value = 1, message = "Time limit must be at least 1 minute")
    private Integer timeLimitMinutes;

    @Min(value = 1, message = "Max attempts must be at least 1")
    private Integer maxAttempts;

    @Min(value = 0, message = "Passing score cannot be negative")
    @Max(value = 100, message = "Passing score cannot exceed 100")
    private Double passingScore;

    private Boolean randomizeQuestions;
    private Boolean showCorrectAnswers;
    private Boolean allowReview;

    private LocalDateTime availableFrom;
    private LocalDateTime availableUntil;
}
