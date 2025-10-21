package com.learningplatform.assignmentservice.controller;

import com.learningplatform.assignmentservice.dto.AssignmentRequestDto;
import com.learningplatform.assignmentservice.dto.AssignmentResponseDto;
import com.learningplatform.assignmentservice.service.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
@Slf4j
public class AssignmentController {

    private final AssignmentService assignmentService;

    // Create a new assignment
    @PostMapping
    public ResponseEntity<AssignmentResponseDto> createAssignment(@Valid @RequestBody AssignmentRequestDto request) {
        log.info("Create assignment request received: {}", request.getTitle());
        AssignmentResponseDto response = assignmentService.createAssignment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get all assignments
    @GetMapping
    public ResponseEntity<List<AssignmentResponseDto>> getAllAssignments() {
        log.info("Get all assignments request");
        List<AssignmentResponseDto> assignments = assignmentService.getAllAssignments();
        return ResponseEntity.ok(assignments);
    }

    // Get assignments by course
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<AssignmentResponseDto>> getAssignmentsByCourse(@PathVariable Long courseId) {
        log.info("Get assignments for course ID: {}", courseId);
        List<AssignmentResponseDto> assignments = assignmentService.getAssignmentsByCourse(courseId);
        return ResponseEntity.ok(assignments);
    }

    // Get published assignments by course (for students)
    @GetMapping("/course/{courseId}/published")
    public ResponseEntity<List<AssignmentResponseDto>> getPublishedAssignmentsByCourse(@PathVariable Long courseId) {
        log.info("Get published assignments for course ID: {}", courseId);
        List<AssignmentResponseDto> assignments = assignmentService.getPublishedAssignmentsByCourse(courseId);
        return ResponseEntity.ok(assignments);
    }

    // Get assignments by lesson
    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<List<AssignmentResponseDto>> getAssignmentsByLesson(@PathVariable Long lessonId) {
        log.info("Get assignments for lesson ID: {}", lessonId);
        List<AssignmentResponseDto> assignments = assignmentService.getAssignmentsByLesson(lessonId);
        return ResponseEntity.ok(assignments);
    }

    // Get assignments by instructor
    @GetMapping("/instructor/{instructorId}")
    public ResponseEntity<List<AssignmentResponseDto>> getAssignmentsByInstructor(@PathVariable Long instructorId) {
        log.info("Get assignments by instructor ID: {}", instructorId);
        List<AssignmentResponseDto> assignments = assignmentService.getAssignmentsByInstructor(instructorId);
        return ResponseEntity.ok(assignments);
    }

    // Get upcoming assignments
    @GetMapping("/upcoming")
    public ResponseEntity<List<AssignmentResponseDto>> getUpcomingAssignments(
            @RequestParam(defaultValue = "7") int daysAhead) {
        log.info("Get upcoming assignments for next {} days", daysAhead);
        List<AssignmentResponseDto> assignments = assignmentService.getUpcomingAssignments(daysAhead);
        return ResponseEntity.ok(assignments);
    }

    // Get assignment by ID
    @GetMapping("/{id}")
    public ResponseEntity<AssignmentResponseDto> getAssignmentById(@PathVariable Long id) {
        log.info("Get assignment request for ID: {}", id);
        AssignmentResponseDto assignment = assignmentService.getAssignmentById(id);
        return ResponseEntity.ok(assignment);
    }

    // Update assignment
    @PutMapping("/{id}")
    public ResponseEntity<AssignmentResponseDto> updateAssignment(
            @PathVariable Long id,
            @Valid @RequestBody AssignmentRequestDto request) {
        log.info("Update assignment request for ID: {}", id);
        AssignmentResponseDto updatedAssignment = assignmentService.updateAssignment(id, request);
        return ResponseEntity.ok(updatedAssignment);
    }

    // Publish assignment
    @PutMapping("/{id}/publish")
    public ResponseEntity<AssignmentResponseDto> publishAssignment(@PathVariable Long id) {
        log.info("Publish assignment request for ID: {}", id);
        AssignmentResponseDto publishedAssignment = assignmentService.publishAssignment(id);
        return ResponseEntity.ok(publishedAssignment);
    }

    // Unpublish assignment
    @PutMapping("/{id}/unpublish")
    public ResponseEntity<AssignmentResponseDto> unpublishAssignment(@PathVariable Long id) {
        log.info("Unpublish assignment request for ID: {}", id);
        AssignmentResponseDto unpublishedAssignment = assignmentService.unpublishAssignment(id);
        return ResponseEntity.ok(unpublishedAssignment);
    }

    // Delete assignment
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
        log.info("Delete assignment request for ID: {}", id);
        assignmentService.deleteAssignment(id);
        return ResponseEntity.noContent().build();
    }
}
