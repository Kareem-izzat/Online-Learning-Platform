package com.learningplatform.progressservice.repository;

import com.learningplatform.progressservice.entity.AssignmentProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface AssignmentProgressRepository extends JpaRepository<AssignmentProgress, Long> {

    List<AssignmentProgress> findByStudentIdAndCourseId(Long studentId, Long courseId);

    @Query("SELECT COUNT(DISTINCT ap.assignmentId) FROM AssignmentProgress ap WHERE ap.studentId = :studentId AND ap.courseId = :courseId AND ap.graded = true")
    Integer countGradedAssignmentsByCourse(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    @Query("SELECT AVG(ap.score) FROM AssignmentProgress ap WHERE ap.studentId = :studentId AND ap.courseId = :courseId AND ap.graded = true")
    BigDecimal getAverageAssignmentScoreByCourse(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    @Query("SELECT COUNT(ap) FROM AssignmentProgress ap WHERE ap.studentId = :studentId AND ap.submitted = true")
    Integer countTotalAssignmentsSubmitted(@Param("studentId") Long studentId);
}
