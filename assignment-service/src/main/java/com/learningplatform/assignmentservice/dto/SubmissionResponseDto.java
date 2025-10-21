package com.learningplatform.assignmentservice.dto;

import com.learningplatform.assignmentservice.entity.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionResponseDto {

    private Long id;
    private Long assignmentId;
    private Long studentId;
    private SubmissionStatus status;
    private String submissionText;
    private String fileUrl;
    private String fileName;
    private Long fileSize;
    private Integer score;
    private String feedback;
    private Integer attemptNumber;
    private LocalDateTime submittedAt;
    private LocalDateTime gradedAt;
    private Long gradedBy;
    private Boolean isLate;
    private Integer timeTakenMinutes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
