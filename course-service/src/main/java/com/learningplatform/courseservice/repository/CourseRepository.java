package com.learningplatform.courseservice.repository;

import com.learningplatform.courseservice.entity.Course;
import com.learningplatform.courseservice.entity.CourseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByInstructorId(Long instructorId);
    List<Course> findByStatus(CourseStatus status);
    List<Course> findByInstructorIdAndStatus(Long instructorId, CourseStatus status);
}
