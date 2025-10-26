package com.learningplatform.progressservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentProgressSummary {
    private Long studentId;
    private Integer totalCoursesEnrolled;
    private Integer coursesCompleted;
    private Integer coursesInProgress;
    private BigDecimal overallCompletionRate;
    private BigDecimal averageQuizScore;
    private BigDecimal averageAssignmentScore;
    private Integer totalLessonsCompleted;
    private Integer totalQuizzesTaken;
    private Integer totalAssignmentsSubmitted;
    private Integer certificatesEarned;
    private List<CourseProgressDto> courseProgress;
}
