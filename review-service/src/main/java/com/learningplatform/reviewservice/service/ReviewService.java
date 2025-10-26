package com.learningplatform.reviewservice.service;

import com.learningplatform.reviewservice.dto.*;
import com.learningplatform.reviewservice.entity.*;
import com.learningplatform.reviewservice.repository.*;
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
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final InstructorResponseRepository instructorResponseRepository;
    private final ReviewHelpfulnessRepository helpfulnessRepository;

    // ===== Review Management =====

    @Transactional
    public ReviewResponse createReview(ReviewRequest request) {
        // Check if student already reviewed this course
        if (reviewRepository.existsByCourseIdAndStudentId(request.getCourseId(), request.getStudentId())) {
            throw new RuntimeException("You have already reviewed this course");
        }

        Review review = Review.builder()
                .courseId(request.getCourseId())
                .studentId(request.getStudentId())
                .studentName(request.getStudentName())
                .rating(request.getRating())
                .comment(request.getComment())
                .status(ReviewStatus.APPROVED) // Auto-approve for now (can add moderation)
                .verified(request.getVerified() != null ? request.getVerified() : false)
                .helpfulCount(0)
                .notHelpfulCount(0)
                .edited(false)
                .build();

        review = reviewRepository.save(review);
        log.info("Review created: id={}, courseId={}, rating={}", review.getId(), review.getCourseId(), review.getRating());

        return mapToResponse(review);
    }

    @Transactional
    public ReviewResponse updateReview(Long reviewId, ReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        // Only allow student who created the review to update
        if (!review.getStudentId().equals(request.getStudentId())) {
            throw new RuntimeException("You can only update your own reviews");
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setEdited(true);
        review.setEditedAt(LocalDateTime.now());

        review = reviewRepository.save(review);
        log.info("Review updated: id={}", reviewId);

        return mapToResponse(review);
    }

    @Transactional
    public void deleteReview(Long reviewId, Long studentId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        if (!review.getStudentId().equals(studentId)) {
            throw new RuntimeException("You can only delete your own reviews");
        }

        reviewRepository.delete(review);
        log.info("Review deleted: id={}", reviewId);
    }

    public ReviewResponse getReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        return mapToResponse(review);
    }

    public List<ReviewResponse> getCourseReviews(Long courseId) {
        return reviewRepository.findByCourseIdAndStatus(courseId, ReviewStatus.APPROVED)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ReviewResponse> getStudentReviews(Long studentId) {
        return reviewRepository.findByStudentId(studentId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ReviewResponse> getRecentReviews(Long courseId) {
        return reviewRepository.findRecentByCourse(courseId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ReviewResponse> getMostHelpfulReviews(Long courseId) {
        return reviewRepository.findMostHelpfulByCourse(courseId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ===== Rating Statistics =====

    public CourseRatingStats getCourseRatingStats(Long courseId) {
        BigDecimal avgRating = reviewRepository.getAverageRatingByCourse(courseId);
        Long totalReviews = reviewRepository.getTotalReviewsByCourse(courseId);

        Long fiveStars = reviewRepository.countByRating(courseId, 5);
        Long fourStars = reviewRepository.countByRating(courseId, 4);
        Long threeStars = reviewRepository.countByRating(courseId, 3);
        Long twoStars = reviewRepository.countByRating(courseId, 2);
        Long oneStars = reviewRepository.countByRating(courseId, 1);

        if (avgRating != null) {
            avgRating = avgRating.setScale(2, RoundingMode.HALF_UP);
        }

        return CourseRatingStats.builder()
                .courseId(courseId)
                .totalReviews(totalReviews != null ? totalReviews : 0L)
                .averageRating(avgRating != null ? avgRating : BigDecimal.ZERO)
                .fiveStarCount(fiveStars != null ? fiveStars : 0L)
                .fourStarCount(fourStars != null ? fourStars : 0L)
                .threeStarCount(threeStars != null ? threeStars : 0L)
                .twoStarCount(twoStars != null ? twoStars : 0L)
                .oneStarCount(oneStars != null ? oneStars : 0L)
                .build();
    }

    // ===== Instructor Responses =====

    @Transactional
    public InstructorResponseDto addInstructorResponse(InstructorResponseRequest request) {
        // Check if review exists
        Review review = reviewRepository.findById(request.getReviewId())
                .orElseThrow(() -> new RuntimeException("Review not found"));

        // Check if response already exists
        if (instructorResponseRepository.existsByReviewId(request.getReviewId())) {
            throw new RuntimeException("Instructor response already exists for this review");
        }

        InstructorResponse response = InstructorResponse.builder()
                .reviewId(request.getReviewId())
                .instructorId(request.getInstructorId())
                .instructorName(request.getInstructorName())
                .response(request.getResponse())
                .edited(false)
                .build();

        response = instructorResponseRepository.save(response);
        log.info("Instructor response added: reviewId={}", request.getReviewId());

        return mapToInstructorResponseDto(response);
    }

    @Transactional
    public InstructorResponseDto updateInstructorResponse(Long responseId, String newResponse) {
        InstructorResponse response = instructorResponseRepository.findById(responseId)
                .orElseThrow(() -> new RuntimeException("Instructor response not found"));

        response.setResponse(newResponse);
        response.setEdited(true);
        response.setEditedAt(LocalDateTime.now());

        response = instructorResponseRepository.save(response);
        log.info("Instructor response updated: id={}", responseId);

        return mapToInstructorResponseDto(response);
    }

    // ===== Review Helpfulness =====

    @Transactional
    public void markReviewHelpful(Long reviewId, Long userId, Boolean helpful) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        // Check if user already voted
        ReviewHelpfulness existingVote = helpfulnessRepository
                .findByReviewIdAndUserId(reviewId, userId)
                .orElse(null);

        if (existingVote != null) {
            // Update existing vote
            Boolean wasHelpful = existingVote.getHelpful();
            if (!wasHelpful.equals(helpful)) {
                // Change vote
                if (wasHelpful) {
                    review.setHelpfulCount(review.getHelpfulCount() - 1);
                    review.setNotHelpfulCount(review.getNotHelpfulCount() + 1);
                } else {
                    review.setNotHelpfulCount(review.getNotHelpfulCount() - 1);
                    review.setHelpfulCount(review.getHelpfulCount() + 1);
                }
                existingVote.setHelpful(helpful);
                helpfulnessRepository.save(existingVote);
            }
        } else {
            // New vote
            if (helpful) {
                review.setHelpfulCount(review.getHelpfulCount() + 1);
            } else {
                review.setNotHelpfulCount(review.getNotHelpfulCount() + 1);
            }

            ReviewHelpfulness helpfulness = ReviewHelpfulness.builder()
                    .reviewId(reviewId)
                    .userId(userId)
                    .helpful(helpful)
                    .build();
            helpfulnessRepository.save(helpfulness);
        }

        reviewRepository.save(review);
        log.info("Review helpfulness updated: reviewId={}, helpful={}", reviewId, helpful);
    }

    // ===== Review Moderation =====

    @Transactional
    public void moderateReview(Long reviewId, ReviewStatus newStatus, String moderatorComment, Long moderatorId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        review.setStatus(newStatus);
        review.setModeratorComment(moderatorComment);
        review.setModeratorId(moderatorId);

        reviewRepository.save(review);
        log.info("Review moderated: id={}, status={}", reviewId, newStatus);
    }

    public List<ReviewResponse> getPendingReviews() {
        return reviewRepository.findByStatus(ReviewStatus.PENDING)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ===== Mappers =====

    private ReviewResponse mapToResponse(Review review) {
        InstructorResponseDto instructorResponse = instructorResponseRepository
                .findByReviewId(review.getId())
                .map(this::mapToInstructorResponseDto)
                .orElse(null);

        return ReviewResponse.builder()
                .id(review.getId())
                .courseId(review.getCourseId())
                .studentId(review.getStudentId())
                .studentName(review.getStudentName())
                .rating(review.getRating())
                .comment(review.getComment())
                .status(review.getStatus().name())
                .verified(review.getVerified())
                .helpfulCount(review.getHelpfulCount())
                .notHelpfulCount(review.getNotHelpfulCount())
                .edited(review.getEdited())
                .editedAt(review.getEditedAt())
                .instructorResponse(instructorResponse)
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

    private InstructorResponseDto mapToInstructorResponseDto(InstructorResponse response) {
        return InstructorResponseDto.builder()
                .id(response.getId())
                .reviewId(response.getReviewId())
                .instructorId(response.getInstructorId())
                .instructorName(response.getInstructorName())
                .response(response.getResponse())
                .edited(response.getEdited())
                .editedAt(response.getEditedAt())
                .createdAt(response.getCreatedAt())
                .updatedAt(response.getUpdatedAt())
                .build();
    }
}
