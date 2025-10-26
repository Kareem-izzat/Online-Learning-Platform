package com.learningplatform.reviewservice.repository;

import com.learningplatform.reviewservice.entity.Review;
import com.learningplatform.reviewservice.entity.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByCourseIdAndStatus(Long courseId, ReviewStatus status);

    List<Review> findByStudentId(Long studentId);

    Optional<Review> findByCourseIdAndStudentId(Long courseId, Long studentId);

    List<Review> findByStatus(ReviewStatus status);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.courseId = :courseId AND r.status = 'APPROVED'")
    BigDecimal getAverageRatingByCourse(@Param("courseId") Long courseId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.courseId = :courseId AND r.status = 'APPROVED'")
    Long getTotalReviewsByCourse(@Param("courseId") Long courseId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.courseId = :courseId AND r.rating = :rating AND r.status = 'APPROVED'")
    Long countByRating(@Param("courseId") Long courseId, @Param("rating") Integer rating);

    @Query("SELECT r FROM Review r WHERE r.courseId = :courseId AND r.status = 'APPROVED' ORDER BY r.createdAt DESC")
    List<Review> findRecentByCourse(@Param("courseId") Long courseId);

    @Query("SELECT r FROM Review r WHERE r.courseId = :courseId AND r.status = 'APPROVED' ORDER BY r.helpfulCount DESC")
    List<Review> findMostHelpfulByCourse(@Param("courseId") Long courseId);

    Boolean existsByCourseIdAndStudentId(Long courseId, Long studentId);
}
