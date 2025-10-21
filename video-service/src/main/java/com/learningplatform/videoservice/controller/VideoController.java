package com.learningplatform.videoservice.controller;

import com.learningplatform.videoservice.dto.VideoRequestDto;
import com.learningplatform.videoservice.dto.VideoResponseDto;
import com.learningplatform.videoservice.service.VideoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
@Slf4j
public class VideoController {

    private final VideoService videoService;

    // POST /upload - Upload a new video (specific path first)
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VideoResponseDto> uploadVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "lessonId", required = false) Long lessonId,
            @RequestParam("uploadedBy") Long uploadedBy) {
        
        log.info("Upload request received for video: {}", title);

        VideoRequestDto request = VideoRequestDto.builder()
                .title(title)
                .description(description)
                .lessonId(lessonId)
                .uploadedBy(uploadedBy)
                .build();

        VideoResponseDto response = videoService.uploadVideo(file, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET / - Get all videos
    @GetMapping
    public ResponseEntity<List<VideoResponseDto>> getAllVideos() {
        log.info("Get all videos request");
        List<VideoResponseDto> videos = videoService.getAllVideos();
        return ResponseEntity.ok(videos);
    }

    // GET /lesson/{lessonId} - Get videos by lesson (specific path before generic /{id})
    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<List<VideoResponseDto>> getVideosByLesson(@PathVariable Long lessonId) {
        log.info("Get videos by lesson request for lesson ID: {}", lessonId);
        List<VideoResponseDto> videos = videoService.getVideosByLesson(lessonId);
        return ResponseEntity.ok(videos);
    }

    // GET /uploader/{uploaderId} - Get videos by uploader (specific path before generic /{id})
    @GetMapping("/uploader/{uploaderId}")
    public ResponseEntity<List<VideoResponseDto>> getVideosByUploader(@PathVariable Long uploaderId) {
        log.info("Get videos by uploader request for uploader ID: {}", uploaderId);
        List<VideoResponseDto> videos = videoService.getVideosByUploader(uploaderId);
        return ResponseEntity.ok(videos);
    }

    // GET /{id}/stream - Stream video (specific path before generic /{id})
    @GetMapping("/{id}/stream")
    public ResponseEntity<byte[]> streamVideo(@PathVariable Long id) {
        log.info("Stream video request for ID: {}", id);
        
        byte[] videoData = videoService.getVideoFile(id);
        VideoResponseDto video = videoService.getVideoById(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
            video.getContentType() != null ? video.getContentType() : "video/mp4"
        ));
        headers.setContentLength(videoData.length);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, 
            "inline; filename=\"" + video.getFileName() + "\"");

        return new ResponseEntity<>(videoData, headers, HttpStatus.OK);
    }

    // POST /{id}/views - Increment view count (specific path before generic /{id})
    @PostMapping("/{id}/views")
    public ResponseEntity<Void> incrementViewCount(@PathVariable Long id) {
        log.info("Increment view count for video ID: {}", id);
        videoService.incrementViewCount(id);
        return ResponseEntity.ok().build();
    }

    // PUT /{id}/metadata - Update metadata (specific path before generic /{id})
    @PutMapping("/{id}/metadata")
    public ResponseEntity<VideoResponseDto> updateMetadata(
            @PathVariable Long id,
            @Valid @RequestBody VideoRequestDto request) {
        log.info("Update metadata request for video ID: {}", id);
        VideoResponseDto updatedVideo = videoService.updateMetadata(id, request);
        return ResponseEntity.ok(updatedVideo);
    }

    // GET /{id} - Get video by ID (generic path last)
    @GetMapping("/{id}")
    public ResponseEntity<VideoResponseDto> getVideoById(@PathVariable Long id) {
        log.info("Get video request for ID: {}", id);
        VideoResponseDto video = videoService.getVideoById(id);
        return ResponseEntity.ok(video);
    }

    // DELETE /{id} - Delete video (generic path last)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVideo(@PathVariable Long id) {
        log.info("Delete video request for ID: {}", id);
        videoService.deleteVideo(id);
        return ResponseEntity.noContent().build();
    }
}
