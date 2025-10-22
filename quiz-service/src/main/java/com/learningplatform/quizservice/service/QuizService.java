package com.learningplatform.quizservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learningplatform.quizservice.dto.*;
import com.learningplatform.quizservice.entity.*;
import com.learningplatform.quizservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final QuizAttemptRepository attemptRepository;
    private final AnswerRepository answerRepository;
    private final ObjectMapper objectMapper;

    // Quiz CRUD
    @Transactional
    public QuizResponseDto createQuiz(QuizRequestDto dto) {
        Quiz quiz = Quiz.builder()
                .courseId(dto.getCourseId())
                .instructorId(dto.getInstructorId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .instructions(dto.getInstructions())
                .status(dto.getStatus() != null ? dto.getStatus() : QuizStatus.DRAFT)
                .timeLimitMinutes(dto.getTimeLimitMinutes())
                .maxAttempts(dto.getMaxAttempts())
                .passingScore(dto.getPassingScore() != null ? dto.getPassingScore() : 70.0)
                .randomizeQuestions(dto.getRandomizeQuestions() != null ? dto.getRandomizeQuestions() : false)
                .showCorrectAnswers(dto.getShowCorrectAnswers() != null ? dto.getShowCorrectAnswers() : true)
                .allowReview(dto.getAllowReview() != null ? dto.getAllowReview() : true)
                .availableFrom(dto.getAvailableFrom())
                .availableUntil(dto.getAvailableUntil())
                .build();

        quiz = quizRepository.save(quiz);
        return mapToResponseDto(quiz);
    }

    public QuizResponseDto getQuizById(Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + id));
        return mapToResponseDto(quiz);
    }

    public List<QuizResponseDto> getQuizzesByCourse(Long courseId) {
        return quizRepository.findByCourseId(courseId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<QuizResponseDto> getAvailableQuizzesByCourse(Long courseId) {
        return quizRepository.findAvailableQuizzesByCourse(courseId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public QuizResponseDto updateQuiz(Long id, QuizRequestDto dto) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        quiz.setTitle(dto.getTitle());
        quiz.setDescription(dto.getDescription());
        quiz.setInstructions(dto.getInstructions());
        if (dto.getStatus() != null) quiz.setStatus(dto.getStatus());
        quiz.setTimeLimitMinutes(dto.getTimeLimitMinutes());
        quiz.setMaxAttempts(dto.getMaxAttempts());
        if (dto.getPassingScore() != null) quiz.setPassingScore(dto.getPassingScore());
        if (dto.getRandomizeQuestions() != null) quiz.setRandomizeQuestions(dto.getRandomizeQuestions());
        if (dto.getShowCorrectAnswers() != null) quiz.setShowCorrectAnswers(dto.getShowCorrectAnswers());
        if (dto.getAllowReview() != null) quiz.setAllowReview(dto.getAllowReview());
        quiz.setAvailableFrom(dto.getAvailableFrom());
        quiz.setAvailableUntil(dto.getAvailableUntil());

        quiz = quizRepository.save(quiz);
        return mapToResponseDto(quiz);
    }

    @Transactional
    public void deleteQuiz(Long id) {
        quizRepository.deleteById(id);
    }

    // Question CRUD
    @Transactional
    public QuestionResponseDto addQuestion(QuestionRequestDto dto) {
        Question question = Question.builder()
                .quizId(dto.getQuizId())
                .type(dto.getType())
                .questionText(dto.getQuestionText())
                .imageUrl(dto.getImageUrl())
                .options(serializeList(dto.getOptions()))
                .correctAnswers(serializeList(dto.getCorrectAnswers()))
                .explanation(dto.getExplanation())
                .points(dto.getPoints() != null ? dto.getPoints() : 1.0)
                .orderIndex(dto.getOrderIndex() != null ? dto.getOrderIndex() : 0)
                .build();

        question = questionRepository.save(question);
        return mapToQuestionResponseDto(question);
    }

    public List<QuestionResponseDto> getQuestionsByQuiz(Long quizId, boolean hideAnswers) {
        return questionRepository.findByQuizIdOrderByOrderIndexAsc(quizId).stream()
                .map(q -> {
                    QuestionResponseDto dto = mapToQuestionResponseDto(q);
                    if (hideAnswers) {
                        dto.setCorrectAnswers(null);
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public QuestionResponseDto updateQuestion(Long id, QuestionRequestDto dto) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        question.setType(dto.getType());
        question.setQuestionText(dto.getQuestionText());
        question.setImageUrl(dto.getImageUrl());
        question.setOptions(serializeList(dto.getOptions()));
        question.setCorrectAnswers(serializeList(dto.getCorrectAnswers()));
        question.setExplanation(dto.getExplanation());
        if (dto.getPoints() != null) question.setPoints(dto.getPoints());
        if (dto.getOrderIndex() != null) question.setOrderIndex(dto.getOrderIndex());

        question = questionRepository.save(question);
        return mapToQuestionResponseDto(question);
    }

    @Transactional
    public void deleteQuestion(Long id) {
        questionRepository.deleteById(id);
    }

    // Quiz Attempt Management
    @Transactional
    public AttemptResponseDto startAttempt(AttemptStartDto dto) {
        Quiz quiz = quizRepository.findById(dto.getQuizId())
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        // Check if quiz is available
        if (quiz.getStatus() != QuizStatus.PUBLISHED) {
            throw new RuntimeException("Quiz is not published");
        }

        // Check attempt limits
        Long attemptCount = attemptRepository.countByQuizIdAndStudentId(dto.getQuizId(), dto.getStudentId());
        if (quiz.getMaxAttempts() != null && attemptCount >= quiz.getMaxAttempts()) {
            throw new RuntimeException("Maximum attempts reached");
        }

        // Check for existing in-progress attempt
        Optional<QuizAttempt> existingAttempt = attemptRepository
                .findByQuizIdAndStudentIdAndStatus(dto.getQuizId(), dto.getStudentId(), AttemptStatus.IN_PROGRESS);
        if (existingAttempt.isPresent()) {
            return mapToAttemptResponseDto(existingAttempt.get());
        }

        // Calculate total points
        Double totalPoints = questionRepository.getTotalPointsByQuizId(dto.getQuizId());

        QuizAttempt attempt = QuizAttempt.builder()
                .quizId(dto.getQuizId())
                .studentId(dto.getStudentId())
                .attemptNumber(attemptCount.intValue() + 1)
                .status(AttemptStatus.IN_PROGRESS)
                .totalPoints(totalPoints != null ? totalPoints : 0.0)
                .build();

        attempt = attemptRepository.save(attempt);
        return mapToAttemptResponseDto(attempt);
    }

    @Transactional
    public void submitAnswer(Long attemptId, AnswerSubmissionDto dto) {
        QuizAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));

        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new RuntimeException("Cannot submit answer - attempt is not in progress");
        }

        Question question = questionRepository.findById(dto.getQuestionId())
                .orElseThrow(() -> new RuntimeException("Question not found"));

        // Check if answer already exists
        Optional<Answer> existingAnswer = answerRepository.findByAttemptIdAndQuestionId(attemptId, dto.getQuestionId());
        Answer answer;

        if (existingAnswer.isPresent()) {
            answer = existingAnswer.get();
        } else {
            answer = Answer.builder()
                    .attemptId(attemptId)
                    .questionId(dto.getQuestionId())
                    .pointsPossible(question.getPoints())
                    .build();
        }

        // Store answer based on question type
        String answerText;
        if (dto.getSelectedOptions() != null && !dto.getSelectedOptions().isEmpty()) {
            answerText = serializeList(dto.getSelectedOptions());
        } else {
            answerText = dto.getTextAnswer();
        }
        answer.setAnswerText(answerText);

        // Auto-grade if possible
        if (question.getType() != QuestionType.SHORT_ANSWER && question.getType() != QuestionType.ESSAY) {
            gradeAnswer(answer, question);
        }

        answerRepository.save(answer);
    }

    @Transactional
    public AttemptResponseDto submitAttempt(Long attemptId) {
        QuizAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));

        attempt.setStatus(AttemptStatus.SUBMITTED);
        attempt.setSubmittedAt(LocalDateTime.now());

        // Calculate time spent
        Duration duration = Duration.between(attempt.getStartedAt(), attempt.getSubmittedAt());
        attempt.setTimeSpentMinutes((int) duration.toMinutes());

        // Auto-grade all answers
        List<Answer> answers = answerRepository.findByAttemptId(attemptId);
        double totalEarnedPoints = 0.0;

        for (Answer answer : answers) {
            Question question = questionRepository.findById(answer.getQuestionId()).orElse(null);
            if (question != null && question.getType() != QuestionType.SHORT_ANSWER && question.getType() != QuestionType.ESSAY) {
                gradeAnswer(answer, question);
                answerRepository.save(answer);
            }
            totalEarnedPoints += answer.getPointsEarned();
        }

        attempt.setEarnedPoints(totalEarnedPoints);

        // Calculate score percentage
        if (attempt.getTotalPoints() > 0) {
            double scorePercentage = (totalEarnedPoints / attempt.getTotalPoints()) * 100;
            attempt.setScore(scorePercentage);
        } else {
            attempt.setScore(0.0);
        }

        // Check if student needs manual grading
        boolean needsManualGrading = answers.stream()
                .anyMatch(a -> {
                    Question q = questionRepository.findById(a.getQuestionId()).orElse(null);
                    return q != null && (q.getType() == QuestionType.SHORT_ANSWER || q.getType() == QuestionType.ESSAY);
                });

        if (!needsManualGrading) {
            attempt.setStatus(AttemptStatus.GRADED);
            attempt.setCompletedAt(LocalDateTime.now());
            
            Quiz quiz = quizRepository.findById(attempt.getQuizId()).orElse(null);
            if (quiz != null) {
                attempt.setPassed(attempt.getScore() >= quiz.getPassingScore());
            }
        }

        attempt = attemptRepository.save(attempt);
        return mapToAttemptResponseDto(attempt);
    }

    public AttemptResponseDto getAttemptById(Long id) {
        QuizAttempt attempt = attemptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));
        return mapToAttemptResponseDto(attempt);
    }

    public List<AttemptResponseDto> getAttemptsByStudent(Long studentId) {
        return attemptRepository.findByStudentId(studentId).stream()
                .map(this::mapToAttemptResponseDto)
                .collect(Collectors.toList());
    }

    public List<AttemptResponseDto> getAttemptsByQuiz(Long quizId) {
        return attemptRepository.findByQuizId(quizId).stream()
                .map(this::mapToAttemptResponseDto)
                .collect(Collectors.toList());
    }

    public Double getAverageScore(Long quizId) {
        Double avg = attemptRepository.getAverageScoreByQuizId(quizId);
        return avg != null ? avg : 0.0;
    }

    // Auto-grading logic
    private void gradeAnswer(Answer answer, Question question) {
        List<String> correctAnswers = deserializeList(question.getCorrectAnswers());
        List<String> studentAnswers = deserializeList(answer.getAnswerText());

        boolean isCorrect = false;
        double pointsEarned = 0.0;

        switch (question.getType()) {
            case MULTIPLE_CHOICE:
            case TRUE_FALSE:
                // Exact match required
                if (studentAnswers.size() == 1 && correctAnswers.size() == 1) {
                    isCorrect = studentAnswers.get(0).equalsIgnoreCase(correctAnswers.get(0));
                    pointsEarned = isCorrect ? question.getPoints() : 0.0;
                }
                break;

            case MULTIPLE_SELECT:
                // All correct answers must be selected, no extras
                Set<String> correctSet = new HashSet<>(correctAnswers.stream().map(String::toLowerCase).collect(Collectors.toList()));
                Set<String> studentSet = new HashSet<>(studentAnswers.stream().map(String::toLowerCase).collect(Collectors.toList()));
                isCorrect = correctSet.equals(studentSet);
                pointsEarned = isCorrect ? question.getPoints() : 0.0;
                break;

            default:
                // SHORT_ANSWER and ESSAY require manual grading
                break;
        }

        answer.setIsCorrect(isCorrect);
        answer.setPointsEarned(pointsEarned);
        answer.setGradedAt(LocalDateTime.now());
    }

    // Mapping helpers
    private QuizResponseDto mapToResponseDto(Quiz quiz) {
        Long questionCount = questionRepository.countByQuizId(quiz.getId());
        Double totalPoints = questionRepository.getTotalPointsByQuizId(quiz.getId());

        return QuizResponseDto.builder()
                .id(quiz.getId())
                .courseId(quiz.getCourseId())
                .instructorId(quiz.getInstructorId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .instructions(quiz.getInstructions())
                .status(quiz.getStatus())
                .timeLimitMinutes(quiz.getTimeLimitMinutes())
                .maxAttempts(quiz.getMaxAttempts())
                .passingScore(quiz.getPassingScore())
                .randomizeQuestions(quiz.getRandomizeQuestions())
                .showCorrectAnswers(quiz.getShowCorrectAnswers())
                .allowReview(quiz.getAllowReview())
                .availableFrom(quiz.getAvailableFrom())
                .availableUntil(quiz.getAvailableUntil())
                .createdAt(quiz.getCreatedAt())
                .updatedAt(quiz.getUpdatedAt())
                .questionCount(questionCount.intValue())
                .totalPoints(totalPoints != null ? totalPoints : 0.0)
                .build();
    }

    private QuestionResponseDto mapToQuestionResponseDto(Question question) {
        return QuestionResponseDto.builder()
                .id(question.getId())
                .quizId(question.getQuizId())
                .type(question.getType())
                .questionText(question.getQuestionText())
                .imageUrl(question.getImageUrl())
                .options(deserializeList(question.getOptions()))
                .correctAnswers(deserializeList(question.getCorrectAnswers()))
                .explanation(question.getExplanation())
                .points(question.getPoints())
                .orderIndex(question.getOrderIndex())
                .createdAt(question.getCreatedAt())
                .updatedAt(question.getUpdatedAt())
                .build();
    }

    private AttemptResponseDto mapToAttemptResponseDto(QuizAttempt attempt) {
        return AttemptResponseDto.builder()
                .id(attempt.getId())
                .quizId(attempt.getQuizId())
                .studentId(attempt.getStudentId())
                .attemptNumber(attempt.getAttemptNumber())
                .status(attempt.getStatus())
                .startedAt(attempt.getStartedAt())
                .submittedAt(attempt.getSubmittedAt())
                .completedAt(attempt.getCompletedAt())
                .score(attempt.getScore())
                .earnedPoints(attempt.getEarnedPoints())
                .totalPoints(attempt.getTotalPoints())
                .passed(attempt.getPassed())
                .timeSpentMinutes(attempt.getTimeSpentMinutes())
                .feedback(attempt.getFeedback())
                .createdAt(attempt.getCreatedAt())
                .updatedAt(attempt.getUpdatedAt())
                .build();
    }

    // JSON serialization helpers
    private String serializeList(List<String> list) {
        if (list == null || list.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            log.error("Error serializing list", e);
            return null;
        }
    }

    private List<String> deserializeList(String json) {
        if (json == null || json.isEmpty()) return new ArrayList<>();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.error("Error deserializing list", e);
            return new ArrayList<>();
        }
    }
}
