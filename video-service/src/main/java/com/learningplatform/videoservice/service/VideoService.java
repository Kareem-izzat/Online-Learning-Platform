package com.learningplatform.videoservice.service;

import com.learningplatform.videoservice.dto.VideoRequestDto;
import com.learningplatform.videoservice.dto.VideoResponseDto;
import com.learningplatform.videoservice.entity.UploadStatus;
import com.learningplatform.videoservice.entity.Video;
import com.learningplatform.videoservice.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoService {

    private final VideoRepository videoRepository;

    @Value("${video.upload.directory:uploads/videos}")
    private String uploadDirectory;

    @Transactional
    public VideoResponseDto uploadVideo(MultipartFile file, VideoRequestDto request) {
        log.info("Uploading video: {} by user: {}", request.getTitle(), request.getUploadedBy());

        // Validate file
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        // Create upload directory if it doesn't exist
        try {
            Path uploadPath = Paths.get(uploadDirectory);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : "";
            String uniqueFilename = UUID.randomUUID().toString() + extension;
            Path filePath = uploadPath.resolve(uniqueFilename);

            // Save file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            log.info("File saved to: {}", filePath.toString());

            // Create video record
            Video video = Video.builder()
                    .lessonId(request.getLessonId())
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .videoUrl(filePath.toString())
                    .fileName(originalFilename)
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .uploadedBy(request.getUploadedBy())
                    .uploadStatus(UploadStatus.COMPLETED)
                    .uploadProgress(100)
                    .publishedAt(LocalDateTime.now())
                    .viewsCount(0L)
                    .build();

            Video savedVideo = videoRepository.save(video);
            log.info("Video saved with ID: {}", savedVideo.getId());

            return mapToResponseDto(savedVideo);

        } catch (IOException e) {
            log.error("Failed to upload video", e);
            throw new RuntimeException("Failed to upload video: " + e.getMessage());
        }
    }

    public VideoResponseDto getVideoById(Long id) {
        log.info("Fetching video with ID: {}", id);
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video not found with ID: " + id));
        return mapToResponseDto(video);
    }

    public List<VideoResponseDto> getVideosByLesson(Long lessonId) {
        log.info("Fetching videos for lesson ID: {}", lessonId);
        return videoRepository.findByLessonId(lessonId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<VideoResponseDto> getVideosByUploader(Long uploaderId) {
        log.info("Fetching videos for uploader ID: {}", uploaderId);
        return videoRepository.findByUploadedBy(uploaderId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<VideoResponseDto> getAllVideos() {
        log.info("Fetching all videos");
        return videoRepository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public VideoResponseDto updateMetadata(Long id, VideoRequestDto request) {
        log.info("Updating metadata for video ID: {}", id);

        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video not found with ID: " + id));

        if (request.getTitle() != null) {
            video.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            video.setDescription(request.getDescription());
        }
        if (request.getLessonId() != null) {
            video.setLessonId(request.getLessonId());
        }

        Video updatedVideo = videoRepository.save(video);
        log.info("Video metadata updated for ID: {}", id);

        return mapToResponseDto(updatedVideo);
    }

    @Transactional
    public void incrementViewCount(Long id) {
        log.info("Incrementing view count for video ID: {}", id);
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video not found with ID: " + id));
        
        video.setViewsCount(video.getViewsCount() + 1);
        videoRepository.save(video);
    }

    @Transactional
    public void deleteVideo(Long id) {
        log.info("Deleting video with ID: {}", id);

        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video not found with ID: " + id));

        // Delete file from filesystem
        try {
            if (video.getVideoUrl() != null) {
                Path filePath = Paths.get(video.getVideoUrl());
                Files.deleteIfExists(filePath);
                log.info("File deleted: {}", filePath.toString());
            }
        } catch (IOException e) {
            log.error("Failed to delete video file", e);
        }

        // Delete database record
        videoRepository.deleteById(id);
        log.info("Video deleted successfully: {}", id);
    }

    public byte[] getVideoFile(Long id) {
        log.info("Fetching video file for ID: {}", id);
        
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video not found with ID: " + id));

        try {
            Path filePath = Paths.get(video.getVideoUrl());
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.error("Failed to read video file", e);
            throw new RuntimeException("Failed to read video file: " + e.getMessage());
        }
    }

    private VideoResponseDto mapToResponseDto(Video video) {
        return VideoResponseDto.builder()
                .id(video.getId())
                .lessonId(video.getLessonId())
                .title(video.getTitle())
                .description(video.getDescription())
                .videoUrl(video.getVideoUrl())
                .thumbnailUrl(video.getThumbnailUrl())
                .durationSeconds(video.getDurationSeconds())
                .fileSize(video.getFileSize())
                .fileName(video.getFileName())
                .contentType(video.getContentType())
                .uploadStatus(video.getUploadStatus())
                .uploadProgress(video.getUploadProgress())
                .errorMessage(video.getErrorMessage())
                .viewsCount(video.getViewsCount())
                .uploadedBy(video.getUploadedBy())
                .createdAt(video.getCreatedAt())
                .updatedAt(video.getUpdatedAt())
                .publishedAt(video.getPublishedAt())
                .build();
    }
}
