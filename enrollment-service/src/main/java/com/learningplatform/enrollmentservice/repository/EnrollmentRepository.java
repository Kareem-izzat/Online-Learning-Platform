package com.learningplatform.enrollmentservice.repository;

import com.learningplatform.enrollmentservice.entity.Enrollment;
import com.learningplatform.enrollmentservice.entity.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByUserId(Long userId);

    List<Enrollment> findByCourseId(Long courseId);

    List<Enrollment> findByStatus(EnrollmentStatus status);

    Optional<Enrollment> findByUserIdAndCourseId(Long userId, Long courseId);

    @Query("SELECT e FROM Enrollment e WHERE e.userId = :userId AND e.status = :status")
    List<Enrollment> findByUserIdAndStatus(Long userId, EnrollmentStatus status);

    @Query("SELECT e FROM Enrollment e WHERE e.courseId = :courseId AND e.status = :status")
    List<Enrollment> findByCourseIdAndStatus(Long courseId, EnrollmentStatus status);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.courseId = :courseId AND e.status = 'ACTIVE'")
    Long countActiveByCourseId(Long courseId);

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
}
