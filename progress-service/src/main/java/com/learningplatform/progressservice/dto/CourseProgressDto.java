package com.learningplatform.progressservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseProgressDto {
    private Long id;
    private Long studentId;
    private Long courseId;
    private Long enrollmentId;
    private BigDecimal completionPercentage;
    private Integer totalLessons;
    private Integer completedLessons;
    private Integer totalQuizzes;
    private Integer completedQuizzes;
    private Integer totalAssignments;
    private Integer completedAssignments;
    private BigDecimal averageQuizScore;
    private BigDecimal averageAssignmentScore;
    private LocalDateTime lastActivityAt;
    private LocalDateTime completedAt;
    private Boolean certificateIssued;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
