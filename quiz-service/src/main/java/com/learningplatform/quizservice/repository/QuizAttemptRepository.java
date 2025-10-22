package com.learningplatform.quizservice.repository;

import com.learningplatform.quizservice.entity.QuizAttempt;
import com.learningplatform.quizservice.entity.AttemptStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    List<QuizAttempt> findByStudentId(Long studentId);

    List<QuizAttempt> findByQuizId(Long quizId);

    List<QuizAttempt> findByQuizIdAndStudentId(Long quizId, Long studentId);

    Optional<QuizAttempt> findByQuizIdAndStudentIdAndStatus(Long quizId, Long studentId, AttemptStatus status);

    Long countByQuizIdAndStudentId(Long quizId, Long studentId);

    @Query("SELECT AVG(qa.score) FROM QuizAttempt qa WHERE qa.quizId = :quizId AND qa.status = 'GRADED'")
    Double getAverageScoreByQuizId(Long quizId);

    @Query("SELECT MAX(qa.score) FROM QuizAttempt qa WHERE qa.quizId = :quizId AND qa.studentId = :studentId AND qa.status = 'GRADED'")
    Double getBestScoreByQuizIdAndStudentId(Long quizId, Long studentId);
}
