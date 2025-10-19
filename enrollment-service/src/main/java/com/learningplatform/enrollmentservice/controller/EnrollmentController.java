package com.learningplatform.enrollmentservice.controller;

import com.learningplatform.enrollmentservice.dto.EnrollmentRequestDto;
import com.learningplatform.enrollmentservice.dto.EnrollmentResponseDto;
import com.learningplatform.enrollmentservice.service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
@Slf4j
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping
    public ResponseEntity<EnrollmentResponseDto> enrollStudent(@Valid @RequestBody EnrollmentRequestDto request) {
        log.info("Received enrollment request for user {} in course {}", request.getUserId(), request.getCourseId());
        EnrollmentResponseDto response = enrollmentService.enrollStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnrollmentResponseDto> getEnrollmentById(@PathVariable Long id) {
        log.info("Received request to get enrollment by ID: {}", id);
        EnrollmentResponseDto response = enrollmentService.getEnrollmentById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<EnrollmentResponseDto>> getEnrollmentsByUserId(@PathVariable Long userId) {
        log.info("Received request to get enrollments for user ID: {}", userId);
        List<EnrollmentResponseDto> response = enrollmentService.getEnrollmentsByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<EnrollmentResponseDto>> getEnrollmentsByCourseId(@PathVariable Long courseId) {
        log.info("Received request to get enrollments for course ID: {}", courseId);
        List<EnrollmentResponseDto> response = enrollmentService.getEnrollmentsByCourseId(courseId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<EnrollmentResponseDto>> getActiveEnrollmentsByUserId(@PathVariable Long userId) {
        log.info("Received request to get active enrollments for user ID: {}", userId);
        List<EnrollmentResponseDto> response = enrollmentService.getActiveEnrollmentsByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/progress")
    public ResponseEntity<EnrollmentResponseDto> updateProgress(
            @PathVariable Long id,
            @RequestParam Integer progressPercentage) {
        log.info("Received request to update progress for enrollment ID: {} to {}%", id, progressPercentage);
        EnrollmentResponseDto response = enrollmentService.updateProgress(id, progressPercentage);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<EnrollmentResponseDto> completeEnrollment(@PathVariable Long id) {
        log.info("Received request to complete enrollment ID: {}", id);
        EnrollmentResponseDto response = enrollmentService.completeEnrollment(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelEnrollment(@PathVariable Long id) {
        log.info("Received request to cancel enrollment ID: {}", id);
        enrollmentService.cancelEnrollment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/course/{courseId}/count")
    public ResponseEntity<Long> getActiveCourseEnrollmentCount(@PathVariable Long courseId) {
        log.info("Received request to count active enrollments for course ID: {}", courseId);
        Long count = enrollmentService.getActiveCourseEnrollmentCount(courseId);
        return ResponseEntity.ok(count);
    }
}
