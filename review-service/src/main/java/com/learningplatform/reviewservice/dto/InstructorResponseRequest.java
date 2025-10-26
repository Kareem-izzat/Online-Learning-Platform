package com.learningplatform.reviewservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstructorResponseRequest {

    @NotNull(message = "Review ID is required")
    private Long reviewId;

    @NotNull(message = "Instructor ID is required")
    private Long instructorId;

    private String instructorName;

    @NotBlank(message = "Response is required")
    @Size(max = 2000, message = "Response must not exceed 2000 characters")
    private String response;
}
