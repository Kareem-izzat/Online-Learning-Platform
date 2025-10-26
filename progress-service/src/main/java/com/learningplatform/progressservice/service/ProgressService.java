package com.learningplatform.progressservice.service;

import com.learningplatform.progressservice.dto.*;
import com.learningplatform.progressservice.entity.*;
import com.learningplatform.progressservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProgressService {

    private final CourseProgressRepository courseProgressRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final QuizProgressRepository quizProgressRepository;
    private final AssignmentProgressRepository assignmentProgressRepository;

    // ===== Lesson Progress =====

    @Transactional
    public LessonProgressDto updateLessonProgress(LessonProgressRequest request) {
        LessonProgress progress = lessonProgressRepository
                .findByStudentIdAndLessonId(request.getStudentId(), request.getLessonId())
                .orElse(LessonProgress.builder()
                        .studentId(request.getStudentId())
                        .courseId(request.getCourseId())
                        .lessonId(request.getLessonId())
                        .completed(false)
                        .startedAt(LocalDateTime.now())
                        .build());

        if (request.getVideoProgressSeconds() != null) {
            progress.setVideoProgressSeconds(request.getVideoProgressSeconds());
        }
        if (request.getVideoDurationSeconds() != null) {
            progress.setVideoDurationSeconds(request.getVideoDurationSeconds());
        }
        if (request.getCompleted() != null && request.getCompleted() && !progress.getCompleted()) {
            progress.setCompleted(true);
            progress.setCompletedAt(LocalDateTime.now());
        }

        progress = lessonProgressRepository.save(progress);

        // Recalculate course progress
        updateCourseProgress(request.getStudentId(), request.getCourseId());

        return mapToLessonDto(progress);
    }

    public List<LessonProgressDto> getLessonProgressByCourse(Long studentId, Long courseId) {
        return lessonProgressRepository.findByStudentIdAndCourseId(studentId, courseId)
                .stream()
                .map(this::mapToLessonDto)
                .collect(Collectors.toList());
    }

    // ===== Quiz Progress =====

    @Transactional
    public void recordQuizProgress(QuizProgressRequest request) {
        QuizProgress progress = QuizProgress.builder()
                .studentId(request.getStudentId())
                .courseId(request.getCourseId())
                .quizId(request.getQuizId())
                .attemptId(request.getAttemptId())
                .completed(true)
                .passed(request.getPassed())
                .score(request.getScore())
                .attemptNumber(request.getAttemptNumber() != null ? request.getAttemptNumber() : 1)
                .completedAt(LocalDateTime.now())
                .build();

        // Update best score if this is a better attempt
        BigDecimal bestScore = quizProgressRepository.getBestScoreForQuiz(
                request.getStudentId(), request.getQuizId());
        if (bestScore == null || request.getScore().compareTo(bestScore) > 0) {
            progress.setBestScore(request.getScore());
        } else {
            progress.setBestScore(bestScore);
        }

        quizProgressRepository.save(progress);

        // Recalculate course progress
        updateCourseProgress(request.getStudentId(), request.getCourseId());
    }

    public List<QuizProgress> getQuizProgressByCourse(Long studentId, Long courseId) {
        return quizProgressRepository.findByStudentIdAndCourseId(studentId, courseId);
    }

    // ===== Assignment Progress =====

    @Transactional
    public void recordAssignmentProgress(AssignmentProgressRequest request) {
        AssignmentProgress progress = AssignmentProgress.builder()
                .studentId(request.getStudentId())
                .courseId(request.getCourseId())
                .assignmentId(request.getAssignmentId())
                .submissionId(request.getSubmissionId())
                .submitted(true)
                .submittedAt(LocalDateTime.now())
                .build();

        if (request.getScore() != null) {
            progress.setGraded(true);
            progress.setScore(request.getScore());
            progress.setGradedAt(LocalDateTime.now());
        }

        if (request.getStatus() != null) {
            try {
                progress.setStatus(SubmissionStatus.valueOf(request.getStatus()));
            } catch (IllegalArgumentException e) {
                progress.setStatus(SubmissionStatus.SUBMITTED);
            }
        }

        assignmentProgressRepository.save(progress);

        // Recalculate course progress
        updateCourseProgress(request.getStudentId(), request.getCourseId());
    }

    public List<AssignmentProgress> getAssignmentProgressByCourse(Long studentId, Long courseId) {
        return assignmentProgressRepository.findByStudentIdAndCourseId(studentId, courseId);
    }

    // ===== Course Progress =====

    @Transactional
    public CourseProgressDto initializeCourseProgress(Long studentId, Long courseId, Long enrollmentId,
                                                       Integer totalLessons, Integer totalQuizzes, Integer totalAssignments) {
        CourseProgress progress = courseProgressRepository
                .findByStudentIdAndCourseId(studentId, courseId)
                .orElse(CourseProgress.builder()
                        .studentId(studentId)
                        .courseId(courseId)
                        .enrollmentId(enrollmentId)
                        .completionPercentage(BigDecimal.ZERO)
                        .totalLessons(totalLessons != null ? totalLessons : 0)
                        .completedLessons(0)
                        .totalQuizzes(totalQuizzes != null ? totalQuizzes : 0)
                        .completedQuizzes(0)
                        .totalAssignments(totalAssignments != null ? totalAssignments : 0)
                        .completedAssignments(0)
                        .certificateIssued(false)
                        .build());

        progress = courseProgressRepository.save(progress);
        return mapToCourseDto(progress);
    }

    @Transactional
    public void updateCourseProgress(Long studentId, Long courseId) {
        CourseProgress progress = courseProgressRepository
                .findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new RuntimeException("Course progress not found"));

        // Count completed items
        Integer completedLessons = lessonProgressRepository
                .countCompletedLessonsByCourse(studentId, courseId);
        Integer completedQuizzes = quizProgressRepository
                .countCompletedQuizzesByCourse(studentId, courseId);
        Integer completedAssignments = assignmentProgressRepository
                .countGradedAssignmentsByCourse(studentId, courseId);

        progress.setCompletedLessons(completedLessons != null ? completedLessons : 0);
        progress.setCompletedQuizzes(completedQuizzes != null ? completedQuizzes : 0);
        progress.setCompletedAssignments(completedAssignments != null ? completedAssignments : 0);

        // Calculate average scores
        BigDecimal avgQuizScore = quizProgressRepository
                .getAverageQuizScoreByCourse(studentId, courseId);
        BigDecimal avgAssignmentScore = assignmentProgressRepository
                .getAverageAssignmentScoreByCourse(studentId, courseId);

        progress.setAverageQuizScore(avgQuizScore);
        progress.setAverageAssignmentScore(avgAssignmentScore);

        // Calculate overall completion percentage
        int totalItems = progress.getTotalLessons() + progress.getTotalQuizzes() + progress.getTotalAssignments();
        int completedItems = progress.getCompletedLessons() + progress.getCompletedQuizzes() + progress.getCompletedAssignments();

        if (totalItems > 0) {
            BigDecimal completion = BigDecimal.valueOf(completedItems)
                    .divide(BigDecimal.valueOf(totalItems), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
            progress.setCompletionPercentage(completion);

            // Check if course is completed
            if (completion.compareTo(BigDecimal.valueOf(100)) == 0 && progress.getCompletedAt() == null) {
                progress.setCompletedAt(LocalDateTime.now());
            }
        }

        courseProgressRepository.save(progress);
    }

    public CourseProgressDto getCourseProgress(Long studentId, Long courseId) {
        CourseProgress progress = courseProgressRepository
                .findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new RuntimeException("Course progress not found"));
        return mapToCourseDto(progress);
    }

    public List<CourseProgressDto> getStudentProgress(Long studentId) {
        return courseProgressRepository.findByStudentId(studentId)
                .stream()
                .map(this::mapToCourseDto)
                .collect(Collectors.toList());
    }

    // ===== Student Summary =====

    public StudentProgressSummary getStudentProgressSummary(Long studentId) {
        List<CourseProgressDto> allProgress = getStudentProgress(studentId);

        long completedCount = allProgress.stream()
                .filter(p -> p.getCompletionPercentage().compareTo(BigDecimal.valueOf(100)) == 0)
                .count();

        long inProgressCount = allProgress.stream()
                .filter(p -> p.getCompletionPercentage().compareTo(BigDecimal.ZERO) > 0
                        && p.getCompletionPercentage().compareTo(BigDecimal.valueOf(100)) < 0)
                .count();

        BigDecimal overallCompletion = allProgress.isEmpty() ? BigDecimal.ZERO :
                allProgress.stream()
                        .map(CourseProgressDto::getCompletionPercentage)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(allProgress.size()), 2, RoundingMode.HALF_UP);

        BigDecimal avgQuizScore = courseProgressRepository.getStudentAverageQuizScore(studentId);
        BigDecimal avgAssignmentScore = courseProgressRepository.getStudentAverageAssignmentScore(studentId);

        Integer totalLessons = lessonProgressRepository.countTotalCompletedLessons(studentId);
        Integer totalQuizzes = quizProgressRepository.countTotalQuizzesTaken(studentId);
        Integer totalAssignments = assignmentProgressRepository.countTotalAssignmentsSubmitted(studentId);

        long certificatesEarned = allProgress.stream()
                .filter(CourseProgressDto::getCertificateIssued)
                .count();

        return StudentProgressSummary.builder()
                .studentId(studentId)
                .totalCoursesEnrolled(allProgress.size())
                .coursesCompleted((int) completedCount)
                .coursesInProgress((int) inProgressCount)
                .overallCompletionRate(overallCompletion)
                .averageQuizScore(avgQuizScore)
                .averageAssignmentScore(avgAssignmentScore)
                .totalLessonsCompleted(totalLessons != null ? totalLessons : 0)
                .totalQuizzesTaken(totalQuizzes != null ? totalQuizzes : 0)
                .totalAssignmentsSubmitted(totalAssignments != null ? totalAssignments : 0)
                .certificatesEarned((int) certificatesEarned)
                .courseProgress(allProgress)
                .build();
    }

    // ===== Mappers =====

    private CourseProgressDto mapToCourseDto(CourseProgress entity) {
        return CourseProgressDto.builder()
                .id(entity.getId())
                .studentId(entity.getStudentId())
                .courseId(entity.getCourseId())
                .enrollmentId(entity.getEnrollmentId())
                .completionPercentage(entity.getCompletionPercentage())
                .totalLessons(entity.getTotalLessons())
                .completedLessons(entity.getCompletedLessons())
                .totalQuizzes(entity.getTotalQuizzes())
                .completedQuizzes(entity.getCompletedQuizzes())
                .totalAssignments(entity.getTotalAssignments())
                .completedAssignments(entity.getCompletedAssignments())
                .averageQuizScore(entity.getAverageQuizScore())
                .averageAssignmentScore(entity.getAverageAssignmentScore())
                .lastActivityAt(entity.getLastActivityAt())
                .completedAt(entity.getCompletedAt())
                .certificateIssued(entity.getCertificateIssued())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private LessonProgressDto mapToLessonDto(LessonProgress entity) {
        return LessonProgressDto.builder()
                .id(entity.getId())
                .studentId(entity.getStudentId())
                .courseId(entity.getCourseId())
                .lessonId(entity.getLessonId())
                .completed(entity.getCompleted())
                .videoProgressSeconds(entity.getVideoProgressSeconds())
                .videoDurationSeconds(entity.getVideoDurationSeconds())
                .startedAt(entity.getStartedAt())
                .completedAt(entity.getCompletedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
