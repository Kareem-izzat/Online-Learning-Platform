package com.learningplatform.reviewservice.repository;

import com.learningplatform.reviewservice.entity.ReviewHelpfulness;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewHelpfulnessRepository extends JpaRepository<ReviewHelpfulness, Long> {

    Optional<ReviewHelpfulness> findByReviewIdAndUserId(Long reviewId, Long userId);

    Boolean existsByReviewIdAndUserId(Long reviewId, Long userId);
}
