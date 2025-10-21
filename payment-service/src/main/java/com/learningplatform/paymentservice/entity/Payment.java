package com.learningplatform.paymentservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;  // Student making payment

    @Column(nullable = false)
    private Long courseId;  // Course being purchased

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;  // Payment amount

    @Column(length = 3)
    private String currency;  // USD, EUR, etc.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(unique = true)
    private String stripePaymentIntentId;  // Stripe Payment Intent ID

    @Column(unique = true)
    private String stripeChargeId;  // Stripe Charge ID

    private String stripeCustomerId;  // Stripe Customer ID

    @Column(length = 1000)
    private String description;

    private String receiptUrl;  // Stripe receipt URL

    @Column(length = 1000)
    private String failureReason;  // Why payment failed

    private LocalDateTime paidAt;  // When payment completed

    private LocalDateTime refundedAt;  // When refunded

    @Column(length = 500)
    private String refundReason;  // Why payment was refunded

    @Column(precision = 10, scale = 2)
    private BigDecimal refundedAmount;  // Amount refunded

    @Column(length = 2000)
    private String metadata;  // Additional info (JSON)

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = PaymentStatus.PENDING;
        }
        if (currency == null) {
            currency = "USD";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (status == PaymentStatus.COMPLETED && paidAt == null) {
            paidAt = LocalDateTime.now();
        }
        if (status == PaymentStatus.REFUNDED && refundedAt == null) {
            refundedAt = LocalDateTime.now();
        }
    }
}
