package com.learningplatform.progressservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizProgressRequest {
    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotNull(message = "Quiz ID is required")
    private Long quizId;

    @NotNull(message = "Attempt ID is required")
    private Long attemptId;

    @NotNull(message = "Score is required")
    private BigDecimal score;

    @NotNull(message = "Passed status is required")
    private Boolean passed;

    private Integer attemptNumber;
}
