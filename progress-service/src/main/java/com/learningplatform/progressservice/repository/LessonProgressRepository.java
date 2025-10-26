package com.learningplatform.progressservice.repository;

import com.learningplatform.progressservice.entity.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {

    Optional<LessonProgress> findByStudentIdAndLessonId(Long studentId, Long lessonId);

    List<LessonProgress> findByStudentIdAndCourseId(Long studentId, Long courseId);

    @Query("SELECT COUNT(lp) FROM LessonProgress lp WHERE lp.studentId = :studentId AND lp.courseId = :courseId AND lp.completed = true")
    Integer countCompletedLessonsByCourse(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    @Query("SELECT COUNT(lp) FROM LessonProgress lp WHERE lp.studentId = :studentId AND lp.completed = true")
    Integer countTotalCompletedLessons(@Param("studentId") Long studentId);
}
