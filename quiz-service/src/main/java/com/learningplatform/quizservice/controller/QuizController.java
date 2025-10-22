package com.learningplatform.quizservice.controller;

import com.learningplatform.quizservice.dto.*;
import com.learningplatform.quizservice.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    // Quiz Management
    @PostMapping
    public ResponseEntity<QuizResponseDto> createQuiz(@Valid @RequestBody QuizRequestDto dto) {
        QuizResponseDto quiz = quizService.createQuiz(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(quiz);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuizResponseDto> getQuiz(@PathVariable Long id) {
        QuizResponseDto quiz = quizService.getQuizById(id);
        return ResponseEntity.ok(quiz);
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<QuizResponseDto>> getQuizzesByCourse(@PathVariable Long courseId) {
        List<QuizResponseDto> quizzes = quizService.getQuizzesByCourse(courseId);
        return ResponseEntity.ok(quizzes);
    }

    @GetMapping("/course/{courseId}/available")
    public ResponseEntity<List<QuizResponseDto>> getAvailableQuizzes(@PathVariable Long courseId) {
        List<QuizResponseDto> quizzes = quizService.getAvailableQuizzesByCourse(courseId);
        return ResponseEntity.ok(quizzes);
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuizResponseDto> updateQuiz(
            @PathVariable Long id,
            @Valid @RequestBody QuizRequestDto dto) {
        QuizResponseDto quiz = quizService.updateQuiz(id, dto);
        return ResponseEntity.ok(quiz);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Long id) {
        quizService.deleteQuiz(id);
        return ResponseEntity.noContent().build();
    }

    // Question Management
    @PostMapping("/questions")
    public ResponseEntity<QuestionResponseDto> addQuestion(@Valid @RequestBody QuestionRequestDto dto) {
        QuestionResponseDto question = quizService.addQuestion(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(question);
    }

    @GetMapping("/{quizId}/questions")
    public ResponseEntity<List<QuestionResponseDto>> getQuestions(
            @PathVariable Long quizId,
            @RequestParam(defaultValue = "false") boolean hideAnswers) {
        List<QuestionResponseDto> questions = quizService.getQuestionsByQuiz(quizId, hideAnswers);
        return ResponseEntity.ok(questions);
    }

    @PutMapping("/questions/{id}")
    public ResponseEntity<QuestionResponseDto> updateQuestion(
            @PathVariable Long id,
            @Valid @RequestBody QuestionRequestDto dto) {
        QuestionResponseDto question = quizService.updateQuestion(id, dto);
        return ResponseEntity.ok(question);
    }

    @DeleteMapping("/questions/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        quizService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }

    // Quiz Attempts
    @PostMapping("/attempts/start")
    public ResponseEntity<AttemptResponseDto> startAttempt(@Valid @RequestBody AttemptStartDto dto) {
        AttemptResponseDto attempt = quizService.startAttempt(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(attempt);
    }

    @PostMapping("/attempts/{attemptId}/answer")
    public ResponseEntity<Void> submitAnswer(
            @PathVariable Long attemptId,
            @Valid @RequestBody AnswerSubmissionDto dto) {
        quizService.submitAnswer(attemptId, dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/attempts/{attemptId}/submit")
    public ResponseEntity<AttemptResponseDto> submitAttempt(@PathVariable Long attemptId) {
        AttemptResponseDto attempt = quizService.submitAttempt(attemptId);
        return ResponseEntity.ok(attempt);
    }

    @GetMapping("/attempts/{id}")
    public ResponseEntity<AttemptResponseDto> getAttempt(@PathVariable Long id) {
        AttemptResponseDto attempt = quizService.getAttemptById(id);
        return ResponseEntity.ok(attempt);
    }

    @GetMapping("/attempts/student/{studentId}")
    public ResponseEntity<List<AttemptResponseDto>> getStudentAttempts(@PathVariable Long studentId) {
        List<AttemptResponseDto> attempts = quizService.getAttemptsByStudent(studentId);
        return ResponseEntity.ok(attempts);
    }

    @GetMapping("/{quizId}/attempts")
    public ResponseEntity<List<AttemptResponseDto>> getQuizAttempts(@PathVariable Long quizId) {
        List<AttemptResponseDto> attempts = quizService.getAttemptsByQuiz(quizId);
        return ResponseEntity.ok(attempts);
    }

    @GetMapping("/{quizId}/average-score")
    public ResponseEntity<Double> getAverageScore(@PathVariable Long quizId) {
        Double avgScore = quizService.getAverageScore(quizId);
        return ResponseEntity.ok(avgScore);
    }
}
