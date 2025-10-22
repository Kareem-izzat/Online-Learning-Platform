package com.learningplatform.quizservice.repository;

import com.learningplatform.quizservice.entity.Quiz;
import com.learningplatform.quizservice.entity.QuizStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    List<Quiz> findByCourseId(Long courseId);

    List<Quiz> findByInstructorId(Long instructorId);

    List<Quiz> findByCourseIdAndStatus(Long courseId, QuizStatus status);

    @Query("SELECT q FROM Quiz q WHERE q.courseId = :courseId AND q.status = 'PUBLISHED' " +
           "AND (q.availableFrom IS NULL OR q.availableFrom <= CURRENT_TIMESTAMP) " +
           "AND (q.availableUntil IS NULL OR q.availableUntil >= CURRENT_TIMESTAMP)")
    List<Quiz> findAvailableQuizzesByCourse(Long courseId);

    Long countByCourseId(Long courseId);
}
