package com.learningplatform.courseservice.controller;

import com.learningplatform.courseservice.dto.CourseRequestDto;
import com.learningplatform.courseservice.dto.CourseResponseDto;
import com.learningplatform.courseservice.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Slf4j
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    public ResponseEntity<CourseResponseDto> createCourse(@Valid @RequestBody CourseRequestDto request) {
        log.info("Received request to create course: {}", request.getTitle());
        CourseResponseDto response = courseService.createCourse(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseResponseDto> getCourseById(@PathVariable Long id) {
        log.info("Received request to get course by ID: {}", id);
        CourseResponseDto response = courseService.getCourseById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<CourseResponseDto>> getAllCourses() {
        log.info("Received request to get all courses");
        List<CourseResponseDto> response = courseService.getAllCourses();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/instructor/{instructorId}")
    public ResponseEntity<List<CourseResponseDto>> getCoursesByInstructor(@PathVariable Long instructorId) {
        log.info("Received request to get courses for instructor ID: {}", instructorId);
        List<CourseResponseDto> response = courseService.getCoursesByInstructor(instructorId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/published")
    public ResponseEntity<List<CourseResponseDto>> getPublishedCourses() {
        log.info("Received request to get all published courses");
        List<CourseResponseDto> response = courseService.getPublishedCourses();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseResponseDto> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseRequestDto request) {
        log.info("Received request to update course with ID: {}", id);
        CourseResponseDto response = courseService.updateCourse(id, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/publish")
    public ResponseEntity<CourseResponseDto> publishCourse(@PathVariable Long id) {
        log.info("Received request to publish course with ID: {}", id);
        CourseResponseDto response = courseService.publishCourse(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        log.info("Received request to delete course with ID: {}", id);
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }
}
