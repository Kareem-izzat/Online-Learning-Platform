package com.learningplatform.progressservice.repository;

import com.learningplatform.progressservice.entity.QuizProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface QuizProgressRepository extends JpaRepository<QuizProgress, Long> {

    List<QuizProgress> findByStudentIdAndCourseId(Long studentId, Long courseId);

    List<QuizProgress> findByStudentIdAndQuizId(Long studentId, Long quizId);

    @Query("SELECT COUNT(DISTINCT qp.quizId) FROM QuizProgress qp WHERE qp.studentId = :studentId AND qp.courseId = :courseId AND qp.completed = true")
    Integer countCompletedQuizzesByCourse(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    @Query("SELECT AVG(qp.score) FROM QuizProgress qp WHERE qp.studentId = :studentId AND qp.courseId = :courseId AND qp.completed = true")
    BigDecimal getAverageQuizScoreByCourse(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    @Query("SELECT MAX(qp.score) FROM QuizProgress qp WHERE qp.studentId = :studentId AND qp.quizId = :quizId")
    BigDecimal getBestScoreForQuiz(@Param("studentId") Long studentId, @Param("quizId") Long quizId);

    @Query("SELECT COUNT(qp) FROM QuizProgress qp WHERE qp.studentId = :studentId AND qp.completed = true")
    Integer countTotalQuizzesTaken(@Param("studentId") Long studentId);
}
