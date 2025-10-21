package com.learningplatform.assignmentservice.service;

import com.learningplatform.assignmentservice.dto.AssignmentRequestDto;
import com.learningplatform.assignmentservice.dto.AssignmentResponseDto;
import com.learningplatform.assignmentservice.entity.Assignment;
import com.learningplatform.assignmentservice.repository.AssignmentRepository;
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
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;

    @Transactional
    public AssignmentResponseDto createAssignment(AssignmentRequestDto request) {
        log.info("Creating assignment: {}", request.getTitle());

        Assignment assignment = Assignment.builder()
                .courseId(request.getCourseId())
                .lessonId(request.getLessonId())
                .title(request.getTitle())
                .description(request.getDescription())
                .instructions(request.getInstructions())
                .type(request.getType())
                .totalPoints(request.getTotalPoints())
                .passingScore(request.getPassingScore())
                .dueDate(request.getDueDate())
                .allowLateSubmission(request.getAllowLateSubmission())
                .latePenaltyPercent(request.getLatePenaltyPercent())
                .maxAttempts(request.getMaxAttempts())
                .timeLimitMinutes(request.getTimeLimitMinutes())
                .createdBy(request.getCreatedBy())
                .published(request.getPublished() != null ? request.getPublished() : false)
                .build();

        Assignment savedAssignment = assignmentRepository.save(assignment);
        log.info("Assignment created with ID: {}", savedAssignment.getId());

        return mapToResponseDto(savedAssignment);
    }

    public AssignmentResponseDto getAssignmentById(Long id) {
        log.info("Fetching assignment with ID: {}", id);
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found with ID: " + id));
        return mapToResponseDto(assignment);
    }

    public List<AssignmentResponseDto> getAllAssignments() {
        log.info("Fetching all assignments");
        return assignmentRepository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<AssignmentResponseDto> getAssignmentsByCourse(Long courseId) {
        log.info("Fetching assignments for course ID: {}", courseId);
        return assignmentRepository.findByCourseId(courseId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<AssignmentResponseDto> getPublishedAssignmentsByCourse(Long courseId) {
        log.info("Fetching published assignments for course ID: {}", courseId);
        return assignmentRepository.findByCourseIdAndPublished(courseId, true).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<AssignmentResponseDto> getAssignmentsByLesson(Long lessonId) {
        log.info("Fetching assignments for lesson ID: {}", lessonId);
        return assignmentRepository.findByLessonId(lessonId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<AssignmentResponseDto> getAssignmentsByInstructor(Long instructorId) {
        log.info("Fetching assignments created by instructor ID: {}", instructorId);
        return assignmentRepository.findByCreatedBy(instructorId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<AssignmentResponseDto> getUpcomingAssignments(int daysAhead) {
        log.info("Fetching upcoming assignments for next {} days", daysAhead);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureDate = now.plusDays(daysAhead);
        return assignmentRepository.findUpcomingAssignments(now, futureDate).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public AssignmentResponseDto updateAssignment(Long id, AssignmentRequestDto request) {
        log.info("Updating assignment ID: {}", id);

        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found with ID: " + id));

        // Update fields
        if (request.getTitle() != null) {
            assignment.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            assignment.setDescription(request.getDescription());
        }
        if (request.getInstructions() != null) {
            assignment.setInstructions(request.getInstructions());
        }
        if (request.getTotalPoints() != null) {
            assignment.setTotalPoints(request.getTotalPoints());
        }
        if (request.getPassingScore() != null) {
            assignment.setPassingScore(request.getPassingScore());
        }
        if (request.getDueDate() != null) {
            assignment.setDueDate(request.getDueDate());
        }
        if (request.getAllowLateSubmission() != null) {
            assignment.setAllowLateSubmission(request.getAllowLateSubmission());
        }
        if (request.getLatePenaltyPercent() != null) {
            assignment.setLatePenaltyPercent(request.getLatePenaltyPercent());
        }
        if (request.getMaxAttempts() != null) {
            assignment.setMaxAttempts(request.getMaxAttempts());
        }
        if (request.getTimeLimitMinutes() != null) {
            assignment.setTimeLimitMinutes(request.getTimeLimitMinutes());
        }
        if (request.getPublished() != null) {
            assignment.setPublished(request.getPublished());
        }

        Assignment updatedAssignment = assignmentRepository.save(assignment);
        log.info("Assignment updated: {}", id);

        return mapToResponseDto(updatedAssignment);
    }

    @Transactional
    public void deleteAssignment(Long id) {
        log.info("Deleting assignment ID: {}", id);
        
        if (!assignmentRepository.existsById(id)) {
            throw new RuntimeException("Assignment not found with ID: " + id);
        }

        assignmentRepository.deleteById(id);
        log.info("Assignment deleted: {}", id);
    }

    @Transactional
    public AssignmentResponseDto publishAssignment(Long id) {
        log.info("Publishing assignment ID: {}", id);

        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found with ID: " + id));

        assignment.setPublished(true);
        Assignment publishedAssignment = assignmentRepository.save(assignment);

        log.info("Assignment published: {}", id);
        return mapToResponseDto(publishedAssignment);
    }

    @Transactional
    public AssignmentResponseDto unpublishAssignment(Long id) {
        log.info("Unpublishing assignment ID: {}", id);

        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found with ID: " + id));

        assignment.setPublished(false);
        Assignment unpublishedAssignment = assignmentRepository.save(assignment);

        log.info("Assignment unpublished: {}", id);
        return mapToResponseDto(unpublishedAssignment);
    }

    private AssignmentResponseDto mapToResponseDto(Assignment assignment) {
        return AssignmentResponseDto.builder()
                .id(assignment.getId())
                .courseId(assignment.getCourseId())
                .lessonId(assignment.getLessonId())
                .title(assignment.getTitle())
                .description(assignment.getDescription())
                .instructions(assignment.getInstructions())
                .type(assignment.getType())
                .totalPoints(assignment.getTotalPoints())
                .passingScore(assignment.getPassingScore())
                .dueDate(assignment.getDueDate())
                .allowLateSubmission(assignment.getAllowLateSubmission())
                .latePenaltyPercent(assignment.getLatePenaltyPercent())
                .maxAttempts(assignment.getMaxAttempts())
                .timeLimitMinutes(assignment.getTimeLimitMinutes())
                .createdBy(assignment.getCreatedBy())
                .published(assignment.getPublished())
                .createdAt(assignment.getCreatedAt())
                .updatedAt(assignment.getUpdatedAt())
                .publishedAt(assignment.getPublishedAt())
                .build();
    }
}
