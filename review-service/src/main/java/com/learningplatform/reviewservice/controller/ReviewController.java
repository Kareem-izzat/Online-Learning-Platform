package com.learningplatform.reviewservice.controller;

import com.learningplatform.reviewservice.dto.*;
import com.learningplatform.reviewservice.entity.ReviewStatus;
import com.learningplatform.reviewservice.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // ===== Review CRUD =====

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody ReviewRequest request) {
        ReviewResponse review = reviewService.createReview(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest request) {
        ReviewResponse review = reviewService.updateReview(id, request);
        return ResponseEntity.ok(review);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long id,
            @RequestParam Long studentId) {
        reviewService.deleteReview(id, studentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponse> getReview(@PathVariable Long id) {
        ReviewResponse review = reviewService.getReview(id);
        return ResponseEntity.ok(review);
    }

    // ===== Query Reviews =====

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<ReviewResponse>> getCourseReviews(@PathVariable Long courseId) {
        List<ReviewResponse> reviews = reviewService.getCourseReviews(courseId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/course/{courseId}/recent")
    public ResponseEntity<List<ReviewResponse>> getRecentReviews(@PathVariable Long courseId) {
        List<ReviewResponse> reviews = reviewService.getRecentReviews(courseId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/course/{courseId}/helpful")
    public ResponseEntity<List<ReviewResponse>> getMostHelpfulReviews(@PathVariable Long courseId) {
        List<ReviewResponse> reviews = reviewService.getMostHelpfulReviews(courseId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<ReviewResponse>> getStudentReviews(@PathVariable Long studentId) {
        List<ReviewResponse> reviews = reviewService.getStudentReviews(studentId);
        return ResponseEntity.ok(reviews);
    }

    // ===== Rating Statistics =====

    @GetMapping("/course/{courseId}/stats")
    public ResponseEntity<CourseRatingStats> getCourseRatingStats(@PathVariable Long courseId) {
        CourseRatingStats stats = reviewService.getCourseRatingStats(courseId);
        return ResponseEntity.ok(stats);
    }

    // ===== Instructor Responses =====

    @PostMapping("/responses")
    public ResponseEntity<InstructorResponseDto> addInstructorResponse(
            @Valid @RequestBody InstructorResponseRequest request) {
        InstructorResponseDto response = reviewService.addInstructorResponse(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/responses/{id}")
    public ResponseEntity<InstructorResponseDto> updateInstructorResponse(
            @PathVariable Long id,
            @RequestParam String response) {
        InstructorResponseDto updated = reviewService.updateInstructorResponse(id, response);
        return ResponseEntity.ok(updated);
    }

    // ===== Review Helpfulness =====

    @PostMapping("/{reviewId}/helpful")
    public ResponseEntity<Void> markReviewHelpful(
            @PathVariable Long reviewId,
            @RequestParam Long userId,
            @RequestParam Boolean helpful) {
        reviewService.markReviewHelpful(reviewId, userId, helpful);
        return ResponseEntity.ok().build();
    }

    // ===== Moderation =====

    @PostMapping("/{reviewId}/moderate")
    public ResponseEntity<Void> moderateReview(
            @PathVariable Long reviewId,
            @RequestParam String status,
            @RequestParam(required = false) String moderatorComment,
            @RequestParam Long moderatorId) {
        ReviewStatus reviewStatus = ReviewStatus.valueOf(status);
        reviewService.moderateReview(reviewId, reviewStatus, moderatorComment, moderatorId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/pending")
    public ResponseEntity<List<ReviewResponse>> getPendingReviews() {
        List<ReviewResponse> reviews = reviewService.getPendingReviews();
        return ResponseEntity.ok(reviews);
    }
}
