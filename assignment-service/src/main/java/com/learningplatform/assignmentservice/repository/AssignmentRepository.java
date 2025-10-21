package com.learningplatform.assignmentservice.repository;

import com.learningplatform.assignmentservice.entity.Assignment;
import com.learningplatform.assignmentservice.entity.AssignmentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    // Find all assignments for a specific course
    List<Assignment> findByCourseId(Long courseId);

    // Find published assignments for a course (students only see published)
    List<Assignment> findByCourseIdAndPublished(Long courseId, Boolean published);

    // Find assignments for a specific lesson
    List<Assignment> findByLessonId(Long lessonId);

    // Find assignments by creator (instructor)
    List<Assignment> findByCreatedBy(Long createdBy);

    // Find assignments by type
    List<Assignment> findByType(AssignmentType type);

    // Find assignments with upcoming due dates
    @Query("SELECT a FROM Assignment a WHERE a.dueDate > :now AND a.dueDate < :futureDate AND a.published = true")
    List<Assignment> findUpcomingAssignments(@Param("now") LocalDateTime now, @Param("futureDate") LocalDateTime futureDate);

    // Find overdue assignments (past due date)
    @Query("SELECT a FROM Assignment a WHERE a.dueDate < :now AND a.published = true")
    List<Assignment> findOverdueAssignments(@Param("now") LocalDateTime now);

    // Count published assignments for a course
    Long countByCourseIdAndPublished(Long courseId, Boolean published);

    // Find assignments by course and type
    List<Assignment> findByCourseIdAndType(Long courseId, AssignmentType type);
}
