package com.learningplatform.courseservice.dto;

import com.learningplatform.courseservice.entity.CourseLevel;
import com.learningplatform.courseservice.entity.CourseStatus;
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
public class CourseResponseDto {

    private Long id;
    private String title;
    private String description;
    private Long instructorId;
    private String thumbnailUrl;
    private BigDecimal price;
    private CourseLevel level;
    private CourseStatus status;
    private Integer durationHours;
    private String language;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
    private Integer totalModules;
    private Integer totalLessons;
}
