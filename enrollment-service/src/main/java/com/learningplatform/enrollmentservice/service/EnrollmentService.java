package com.learningplatform.enrollmentservice.service;

import com.learningplatform.enrollmentservice.dto.EnrollmentRequestDto;
import com.learningplatform.enrollmentservice.dto.EnrollmentResponseDto;
import com.learningplatform.enrollmentservice.entity.Enrollment;
import com.learningplatform.enrollmentservice.entity.EnrollmentStatus;
import com.learningplatform.enrollmentservice.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;

    @Transactional
    public EnrollmentResponseDto enrollStudent(EnrollmentRequestDto request) {
        log.info("Enrolling user {} in course {}", request.getUserId(), request.getCourseId());

        // Check if already enrolled
        if (enrollmentRepository.existsByUserIdAndCourseId(request.getUserId(), request.getCourseId())) {
            throw new RuntimeException("User is already enrolled in this course");
        }

        // TODO: Add Feign client calls to verify:
        // 1. User exists in user-service
        // 2. Course exists and is published in course-service

        Enrollment enrollment = Enrollment.builder()
                .userId(request.getUserId())
                .courseId(request.getCourseId())
                .status(EnrollmentStatus.ACTIVE)
                .progressPercentage(0)
                .certificateIssued(false)
                .build();

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        log.info("Successfully enrolled user {} in course {}", request.getUserId(), request.getCourseId());

        return mapToResponseDto(savedEnrollment);
    }

    public EnrollmentResponseDto getEnrollmentById(Long id) {
        log.info("Fetching enrollment with ID: {}", id);
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Enrollment not found with ID: " + id));
        return mapToResponseDto(enrollment);
    }

    public List<EnrollmentResponseDto> getEnrollmentsByUserId(Long userId) {
        log.info("Fetching enrollments for user ID: {}", userId);
        return enrollmentRepository.findByUserId(userId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<EnrollmentResponseDto> getEnrollmentsByCourseId(Long courseId) {
        log.info("Fetching enrollments for course ID: {}", courseId);
        return enrollmentRepository.findByCourseId(courseId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<EnrollmentResponseDto> getActiveEnrollmentsByUserId(Long userId) {
        log.info("Fetching active enrollments for user ID: {}", userId);
        return enrollmentRepository.findByUserIdAndStatus(userId, EnrollmentStatus.ACTIVE).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public EnrollmentResponseDto updateProgress(Long id, Integer progressPercentage) {
        log.info("Updating progress for enrollment ID: {} to {}%", id, progressPercentage);

        if (progressPercentage < 0 || progressPercentage > 100) {
            throw new RuntimeException("Progress percentage must be between 0 and 100");
        }

        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Enrollment not found with ID: " + id));

        enrollment.setProgressPercentage(progressPercentage);
        enrollment.setLastAccessedAt(LocalDateTime.now());

        // Auto-complete if progress reaches 100%
        if (progressPercentage == 100 && enrollment.getStatus() == EnrollmentStatus.ACTIVE) {
            enrollment.setStatus(EnrollmentStatus.COMPLETED);
            enrollment.setCompletionDate(LocalDateTime.now());
            enrollment.setCertificateIssued(true);
            log.info("Enrollment ID: {} marked as COMPLETED", id);
        }

        Enrollment updatedEnrollment = enrollmentRepository.save(enrollment);
        return mapToResponseDto(updatedEnrollment);
    }

    @Transactional
    public EnrollmentResponseDto completeEnrollment(Long id) {
        log.info("Completing enrollment ID: {}", id);

        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Enrollment not found with ID: " + id));

        enrollment.setStatus(EnrollmentStatus.COMPLETED);
        enrollment.setCompletionDate(LocalDateTime.now());
        enrollment.setProgressPercentage(100);
        enrollment.setCertificateIssued(true);

        Enrollment updatedEnrollment = enrollmentRepository.save(enrollment);
        log.info("Enrollment ID: {} marked as COMPLETED", id);

        return mapToResponseDto(updatedEnrollment);
    }

    @Transactional
    public void cancelEnrollment(Long id) {
        log.info("Cancelling enrollment ID: {}", id);

        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Enrollment not found with ID: " + id));

        enrollment.setStatus(EnrollmentStatus.CANCELLED);
        enrollmentRepository.save(enrollment);

        log.info("Enrollment ID: {} cancelled successfully", id);
    }

    public Long getActiveCourseEnrollmentCount(Long courseId) {
        return enrollmentRepository.countActiveByCourseId(courseId);
    }

    private EnrollmentResponseDto mapToResponseDto(Enrollment enrollment) {
        return EnrollmentResponseDto.builder()
                .id(enrollment.getId())
                .userId(enrollment.getUserId())
                .courseId(enrollment.getCourseId())
                .status(enrollment.getStatus())
                .enrollmentDate(enrollment.getEnrollmentDate())
                .completionDate(enrollment.getCompletionDate())
                .progressPercentage(enrollment.getProgressPercentage())
                .lastAccessedAt(enrollment.getLastAccessedAt())
                .certificateIssued(enrollment.getCertificateIssued())
                .createdAt(enrollment.getCreatedAt())
                .updatedAt(enrollment.getUpdatedAt())
                .build();
    }
}
