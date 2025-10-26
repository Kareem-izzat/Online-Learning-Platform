package com.learningplatform.reviewservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private Long id;
    private Long courseId;
    private Long studentId;
    private String studentName;
    private Integer rating;
    private String comment;
    private String status;
    private Boolean verified;
    private Integer helpfulCount;
    private Integer notHelpfulCount;
    private Boolean edited;
    private LocalDateTime editedAt;
    private InstructorResponseDto instructorResponse;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
