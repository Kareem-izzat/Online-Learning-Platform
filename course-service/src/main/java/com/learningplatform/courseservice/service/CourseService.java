package com.learningplatform.courseservice.service;

import com.learningplatform.courseservice.dto.CourseRequestDto;
import com.learningplatform.courseservice.dto.CourseResponseDto;
import com.learningplatform.courseservice.entity.Course;
import com.learningplatform.courseservice.entity.CourseStatus;
import com.learningplatform.courseservice.repository.CourseRepository;
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
@Transactional
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseResponseDto createCourse(CourseRequestDto request) {
        log.info("Creating new course: {}", request.getTitle());
        
        Course course = Course.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .instructorId(request.getInstructorId())
                .thumbnailUrl(request.getThumbnailUrl())
                .price(request.getPrice())
                .level(request.getLevel())
                .status(CourseStatus.DRAFT)
                .durationHours(request.getDurationHours())
                .language(request.getLanguage())
                .build();

        Course savedCourse = courseRepository.save(course);
        log.info("Course created successfully with ID: {}", savedCourse.getId());
        
        return mapToResponseDto(savedCourse);
    }

    @Transactional(readOnly = true)
    public CourseResponseDto getCourseById(Long id) {
        log.info("Fetching course with ID: {}", id);
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + id));
        return mapToResponseDto(course);
    }

    @Transactional(readOnly = true)
    public List<CourseResponseDto> getAllCourses() {
        log.info("Fetching all courses");
        return courseRepository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CourseResponseDto> getCoursesByInstructor(Long instructorId) {
        log.info("Fetching courses for instructor ID: {}", instructorId);
        return courseRepository.findByInstructorId(instructorId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CourseResponseDto> getPublishedCourses() {
        log.info("Fetching all published courses");
        return courseRepository.findByStatus(CourseStatus.PUBLISHED).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public CourseResponseDto updateCourse(Long id, CourseRequestDto request) {
        log.info("Updating course with ID: {}", id);
        
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + id));

        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setThumbnailUrl(request.getThumbnailUrl());
        course.setPrice(request.getPrice());
        course.setLevel(request.getLevel());
        course.setDurationHours(request.getDurationHours());
        course.setLanguage(request.getLanguage());

        Course updatedCourse = courseRepository.save(course);
        log.info("Course updated successfully with ID: {}", updatedCourse.getId());
        
        return mapToResponseDto(updatedCourse);
    }

    public CourseResponseDto publishCourse(Long id) {
        log.info("Publishing course with ID: {}", id);
        
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + id));

        course.setStatus(CourseStatus.PUBLISHED);
        course.setPublishedAt(LocalDateTime.now());

        Course publishedCourse = courseRepository.save(course);
        log.info("Course published successfully with ID: {}", publishedCourse.getId());
        
        return mapToResponseDto(publishedCourse);
    }

    public void deleteCourse(Long id) {
        log.info("Deleting course with ID: {}", id);
        
        if (!courseRepository.existsById(id)) {
            throw new RuntimeException("Course not found with ID: " + id);
        }
        
        courseRepository.deleteById(id);
        log.info("Course deleted successfully with ID: {}", id);
    }

    private CourseResponseDto mapToResponseDto(Course course) {
        return CourseResponseDto.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .instructorId(course.getInstructorId())
                .thumbnailUrl(course.getThumbnailUrl())
                .price(course.getPrice())
                .level(course.getLevel())
                .status(course.getStatus())
                .durationHours(course.getDurationHours())
                .language(course.getLanguage())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .publishedAt(course.getPublishedAt())
                .totalModules(course.getModules() != null ? course.getModules().size() : 0)
                .totalLessons(course.getModules() != null ? 
                    course.getModules().stream()
                        .mapToInt(m -> m.getLessons() != null ? m.getLessons().size() : 0)
                        .sum() : 0)
                .build();
    }
}
