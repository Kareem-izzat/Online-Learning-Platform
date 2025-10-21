package com.learningplatform.assignmentservice.dto;

import com.learningplatform.assignmentservice.entity.AssignmentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentResponseDto {

    private Long id;
    private Long courseId;
    private Long lessonId;
    private String title;
    private String description;
    private String instructions;
    private AssignmentType type;
    private Integer totalPoints;
    private Integer passingScore;
    private LocalDateTime dueDate;
    private Boolean allowLateSubmission;
    private Integer latePenaltyPercent;
    private Integer maxAttempts;
    private Integer timeLimitMinutes;
    private Long createdBy;
    private Boolean published;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
}
