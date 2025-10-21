package com.learningplatform.videoservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoRequestDto {

    private Long lessonId;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Uploader ID is required")
    private Long uploadedBy;
}
