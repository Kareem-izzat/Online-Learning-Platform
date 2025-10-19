package com.learningplatform.enrollmentservice.dto;

import com.learningplatform.enrollmentservice.entity.EnrollmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentResponseDto {

    private Long id;
    private Long userId;
    private Long courseId;
    private EnrollmentStatus status;
    private LocalDateTime enrollmentDate;
    private LocalDateTime completionDate;
    private Integer progressPercentage;
    private LocalDateTime lastAccessedAt;
    private Boolean certificateIssued;
    
    // Optional: Include user and course details from other services
    private String userName;
    private String courseTitle;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
