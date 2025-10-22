package com.learningplatform.quizservice.repository;

import com.learningplatform.quizservice.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByQuizIdOrderByOrderIndexAsc(Long quizId);

    Long countByQuizId(Long quizId);

    @Query("SELECT SUM(q.points) FROM Question q WHERE q.quizId = :quizId")
    Double getTotalPointsByQuizId(Long quizId);

    void deleteByQuizId(Long quizId);
}
