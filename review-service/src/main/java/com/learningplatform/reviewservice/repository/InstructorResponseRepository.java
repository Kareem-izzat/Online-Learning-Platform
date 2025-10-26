package com.learningplatform.reviewservice.repository;

import com.learningplatform.reviewservice.entity.InstructorResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InstructorResponseRepository extends JpaRepository<InstructorResponse, Long> {

    Optional<InstructorResponse> findByReviewId(Long reviewId);

    Boolean existsByReviewId(Long reviewId);
}
