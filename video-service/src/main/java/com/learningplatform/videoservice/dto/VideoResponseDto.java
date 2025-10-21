package com.learningplatform.videoservice.dto;

import com.learningplatform.videoservice.entity.UploadStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoResponseDto {

    private Long id;
    private Long lessonId;
    private String title;
    private String description;
    private String videoUrl;
    private String thumbnailUrl;
    private Integer durationSeconds;
    private Long fileSize;
    private String fileName;
    private String contentType;
    private UploadStatus uploadStatus;
    private Integer uploadProgress;
    private String errorMessage;
    private Long viewsCount;
    private Long uploadedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
}
