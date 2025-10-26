package com.learningplatform.progressservice.repository;

import com.learningplatform.progressservice.entity.CourseProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseProgressRepository extends JpaRepository<CourseProgress, Long> {

    Optional<CourseProgress> findByStudentIdAndCourseId(Long studentId, Long courseId);

    List<CourseProgress> findByStudentId(Long studentId);

    List<CourseProgress> findByCourseId(Long courseId);

    @Query("SELECT cp FROM CourseProgress cp WHERE cp.studentId = :studentId AND cp.completionPercentage = 100")
    List<CourseProgress> findCompletedCoursesByStudent(@Param("studentId") Long studentId);

    @Query("SELECT cp FROM CourseProgress cp WHERE cp.studentId = :studentId AND cp.completionPercentage > 0 AND cp.completionPercentage < 100")
    List<CourseProgress> findInProgressCoursesByStudent(@Param("studentId") Long studentId);

    @Query("SELECT AVG(cp.completionPercentage) FROM CourseProgress cp WHERE cp.courseId = :courseId")
    BigDecimal getAverageCompletionByCourse(@Param("courseId") Long courseId);

    @Query("SELECT COUNT(cp) FROM CourseProgress cp WHERE cp.courseId = :courseId AND cp.completionPercentage = 100")
    Long countCompletedStudentsByCourse(@Param("courseId") Long courseId);

    @Query("SELECT AVG(cp.averageQuizScore) FROM CourseProgress cp WHERE cp.studentId = :studentId")
    BigDecimal getStudentAverageQuizScore(@Param("studentId") Long studentId);

    @Query("SELECT AVG(cp.averageAssignmentScore) FROM CourseProgress cp WHERE cp.studentId = :studentId")
    BigDecimal getStudentAverageAssignmentScore(@Param("studentId") Long studentId);
}
