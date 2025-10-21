package com.learningplatform.assignmentservice.dto;

import com.learningplatform.assignmentservice.entity.AssignmentType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentRequestDto {

    @NotNull(message = "Course ID is required")
    private Long courseId;

    private Long lessonId;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private String instructions;

    @NotNull(message = "Assignment type is required")
    private AssignmentType type;

    @NotNull(message = "Total points is required")
    @Min(value = 1, message = "Total points must be at least 1")
    private Integer totalPoints;

    private Integer passingScore;

    private LocalDateTime dueDate;

    private Boolean allowLateSubmission;

    private Integer latePenaltyPercent;

    private Integer maxAttempts;

    private Integer timeLimitMinutes;

    @NotNull(message = "Creator ID is required")
    private Long createdBy;

    private Boolean published;
}
