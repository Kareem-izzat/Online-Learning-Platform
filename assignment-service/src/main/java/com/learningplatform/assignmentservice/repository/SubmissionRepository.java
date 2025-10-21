package com.learningplatform.assignmentservice.repository;

import com.learningplatform.assignmentservice.entity.Submission;
import com.learningplatform.assignmentservice.entity.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    // Find all submissions for a specific assignment
    List<Submission> findByAssignmentId(Long assignmentId);

    // Find all submissions by a specific student
    List<Submission> findByStudentId(Long studentId);

    // Find submission for a specific assignment and student
    Optional<Submission> findByAssignmentIdAndStudentId(Long assignmentId, Long studentId);

    // Find all submissions by a student with a specific status
    List<Submission> findByStudentIdAndStatus(Long studentId, SubmissionStatus status);

    // Find all submissions for an assignment with a specific status
    List<Submission> findByAssignmentIdAndStatus(Long assignmentId, SubmissionStatus status);

    // Find late submissions
    List<Submission> findByIsLate(Boolean isLate);

    // Find graded submissions for a student
    List<Submission> findByStudentIdAndStatusIn(Long studentId, List<SubmissionStatus> statuses);

    // Count submissions for an assignment
    Long countByAssignmentId(Long assignmentId);

    // Count graded submissions for an assignment
    Long countByAssignmentIdAndStatus(Long assignmentId, SubmissionStatus status);

    // Get average score for an assignment
    @Query("SELECT AVG(s.score) FROM Submission s WHERE s.assignmentId = :assignmentId AND s.score IS NOT NULL")
    Double getAverageScoreForAssignment(@Param("assignmentId") Long assignmentId);

    // Get student's highest score for an assignment (if multiple attempts)
    @Query("SELECT MAX(s.score) FROM Submission s WHERE s.assignmentId = :assignmentId AND s.studentId = :studentId")
    Integer getHighestScoreForStudent(@Param("assignmentId") Long assignmentId, @Param("studentId") Long studentId);

    // Count submissions by a student
    Long countByStudentId(Long studentId);
}
