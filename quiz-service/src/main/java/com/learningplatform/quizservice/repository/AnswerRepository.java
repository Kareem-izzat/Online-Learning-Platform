package com.learningplatform.quizservice.repository;

import com.learningplatform.quizservice.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {

    List<Answer> findByAttemptId(Long attemptId);

    Optional<Answer> findByAttemptIdAndQuestionId(Long attemptId, Long questionId);

    Long countByAttemptId(Long attemptId);

    void deleteByAttemptId(Long attemptId);
}
