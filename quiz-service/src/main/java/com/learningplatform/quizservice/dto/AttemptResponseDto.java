package com.learningplatform.quizservice.dto;

import com.learningplatform.quizservice.entity.AttemptStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttemptResponseDto {

    private Long id;
    private Long quizId;
    private Long studentId;
    private Integer attemptNumber;
    private AttemptStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
    private LocalDateTime completedAt;
    private Double score;
    private Double earnedPoints;
    private Double totalPoints;
    private Boolean passed;
    private Integer timeSpentMinutes;
    private String feedback;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
