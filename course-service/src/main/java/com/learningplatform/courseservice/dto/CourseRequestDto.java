package com.learningplatform.courseservice.dto;

import com.learningplatform.courseservice.entity.CourseLevel;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseRequestDto {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotNull(message = "Instructor ID is required")
    private Long instructorId;

    private String thumbnailUrl;

    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be >= 0")
    private BigDecimal price;

    @NotNull(message = "Course level is required")
    private CourseLevel level;

    @Min(value = 0, message = "Duration must be >= 0")
    private Integer durationHours;

    private String language;
}
