package com.learningplatform.progressservice.controller;

import com.learningplatform.progressservice.dto.*;
import com.learningplatform.progressservice.entity.AssignmentProgress;
import com.learningplatform.progressservice.entity.QuizProgress;
import com.learningplatform.progressservice.service.ProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    // ===== Course Progress Endpoints =====

    @PostMapping("/courses/initialize")
    public ResponseEntity<CourseProgressDto> initializeCourseProgress(
            @RequestParam Long studentId,
            @RequestParam Long courseId,
            @RequestParam Long enrollmentId,
            @RequestParam(required = false) Integer totalLessons,
            @RequestParam(required = false) Integer totalQuizzes,
            @RequestParam(required = false) Integer totalAssignments) {
        CourseProgressDto progress = progressService.initializeCourseProgress(
                studentId, courseId, enrollmentId, totalLessons, totalQuizzes, totalAssignments);
        return ResponseEntity.status(HttpStatus.CREATED).body(progress);
    }

    @GetMapping("/courses/{studentId}/{courseId}")
    public ResponseEntity<CourseProgressDto> getCourseProgress(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {
        CourseProgressDto progress = progressService.getCourseProgress(studentId, courseId);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/students/{studentId}")
    public ResponseEntity<List<CourseProgressDto>> getStudentProgress(@PathVariable Long studentId) {
        List<CourseProgressDto> progress = progressService.getStudentProgress(studentId);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/students/{studentId}/summary")
    public ResponseEntity<StudentProgressSummary> getStudentSummary(@PathVariable Long studentId) {
        StudentProgressSummary summary = progressService.getStudentProgressSummary(studentId);
        return ResponseEntity.ok(summary);
    }

    // ===== Lesson Progress Endpoints =====

    @PostMapping("/lessons")
    public ResponseEntity<LessonProgressDto> updateLessonProgress(
            @Valid @RequestBody LessonProgressRequest request) {
        LessonProgressDto progress = progressService.updateLessonProgress(request);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/lessons/{studentId}/{courseId}")
    public ResponseEntity<List<LessonProgressDto>> getLessonProgress(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {
        List<LessonProgressDto> progress = progressService.getLessonProgressByCourse(studentId, courseId);
        return ResponseEntity.ok(progress);
    }

    // ===== Quiz Progress Endpoints =====

    @PostMapping("/quizzes")
    public ResponseEntity<Void> recordQuizProgress(@Valid @RequestBody QuizProgressRequest request) {
        progressService.recordQuizProgress(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/quizzes/{studentId}/{courseId}")
    public ResponseEntity<List<QuizProgress>> getQuizProgress(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {
        List<QuizProgress> progress = progressService.getQuizProgressByCourse(studentId, courseId);
        return ResponseEntity.ok(progress);
    }

    // ===== Assignment Progress Endpoints =====

    @PostMapping("/assignments")
    public ResponseEntity<Void> recordAssignmentProgress(@Valid @RequestBody AssignmentProgressRequest request) {
        progressService.recordAssignmentProgress(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/assignments/{studentId}/{courseId}")
    public ResponseEntity<List<AssignmentProgress>> getAssignmentProgress(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {
        List<AssignmentProgress> progress = progressService.getAssignmentProgressByCourse(studentId, courseId);
        return ResponseEntity.ok(progress);
    }
}
