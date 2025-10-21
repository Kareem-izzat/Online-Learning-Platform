package com.learningplatform.paymentservice.repository;

import com.learningplatform.paymentservice.entity.Payment;
import com.learningplatform.paymentservice.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByUserId(Long userId);
    
    List<Payment> findByCourseId(Long courseId);
    
    List<Payment> findByUserIdAndStatus(Long userId, PaymentStatus status);
    
    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED'")
    java.math.BigDecimal getTotalRevenue();
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.courseId = :courseId AND p.status = 'COMPLETED'")
    java.math.BigDecimal getRevenueByCourse(Long courseId);
    
    List<Payment> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
