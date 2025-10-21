package com.learningplatform.paymentservice.dto;

import com.learningplatform.paymentservice.entity.PaymentMethod;
import com.learningplatform.paymentservice.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDto {

    private Long id;
    private Long userId;
    private Long courseId;
    private BigDecimal amount;
    private String currency;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String stripePaymentIntentId;
    private String stripeChargeId;
    private String description;
    private String receiptUrl;
    private String failureReason;
    private String refundReason;
    private BigDecimal refundedAmount;
    private String metadata;
    private String clientSecret;  // For Stripe client-side confirmation
    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
