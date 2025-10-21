package com.learningplatform.paymentservice.service;

import com.learningplatform.paymentservice.dto.PaymentRequestDto;
import com.learningplatform.paymentservice.dto.PaymentResponseDto;
import com.learningplatform.paymentservice.dto.RefundRequestDto;
import com.learningplatform.paymentservice.entity.Payment;
import com.learningplatform.paymentservice.entity.PaymentStatus;
import com.learningplatform.paymentservice.repository.PaymentRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    @Transactional
    public PaymentResponseDto createPayment(PaymentRequestDto request) throws StripeException {
        // Create Stripe PaymentIntent
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(convertToStripeAmount(request.getAmount().doubleValue()))
                .setCurrency("usd")
                .setDescription("Course purchase: " + request.getCourseId())
                .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);

        // Save payment record
        Payment payment = Payment.builder()
                .userId(request.getUserId())
                .courseId(request.getCourseId())
                .amount(request.getAmount())
                .currency("USD")
                .paymentMethod(request.getPaymentMethod())
                .status(PaymentStatus.PENDING)
                .stripePaymentIntentId(paymentIntent.getId())
                .build();

        Payment saved = paymentRepository.save(payment);
        log.info("Payment created: {}", saved.getId());

        return mapToResponseDto(saved, paymentIntent.getClientSecret());
    }

    @Transactional
    public PaymentResponseDto confirmPayment(String paymentIntentId) {
        Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setStatus(PaymentStatus.COMPLETED);
        Payment updated = paymentRepository.save(payment);

        log.info("Payment confirmed: {}", updated.getId());
        return mapToResponseDto(updated, null);
    }

    @Transactional
    public PaymentResponseDto refundPayment(Long paymentId, RefundRequestDto request) throws StripeException {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new RuntimeException("Only completed payments can be refunded");
        }

        // Create Stripe refund
        RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(payment.getStripePaymentIntentId())
                .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
                .build();

        Refund refund = Refund.create(params);

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundReason(request.getReason());
        Payment updated = paymentRepository.save(payment);

        log.info("Payment refunded: {}", updated.getId());
        return mapToResponseDto(updated, null);
    }

    public List<PaymentResponseDto> getUserPayments(Long userId) {
        return paymentRepository.findByUserId(userId)
                .stream()
                .map(p -> mapToResponseDto(p, null))
                .collect(Collectors.toList());
    }

    public PaymentResponseDto getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        return mapToResponseDto(payment, null);
    }

    public BigDecimal getTotalRevenue() {
        BigDecimal revenue = paymentRepository.getTotalRevenue();
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    public BigDecimal getRevenueByCourse(Long courseId) {
        BigDecimal revenue = paymentRepository.getRevenueByCourse(courseId);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    private Long convertToStripeAmount(Double amount) {
        return (long) (amount * 100); // Stripe uses cents
    }

    private PaymentResponseDto mapToResponseDto(Payment payment, String clientSecret) {
        return PaymentResponseDto.builder()
                .id(payment.getId())
                .userId(payment.getUserId())
                .courseId(payment.getCourseId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .stripePaymentIntentId(payment.getStripePaymentIntentId())
                .stripeChargeId(payment.getStripeChargeId())
                .description(payment.getDescription())
                .receiptUrl(payment.getReceiptUrl())
                .failureReason(payment.getFailureReason())
                .refundReason(payment.getRefundReason())
                .refundedAmount(payment.getRefundedAmount())
                .metadata(payment.getMetadata())
                .clientSecret(clientSecret)
                .paidAt(payment.getPaidAt())
                .refundedAt(payment.getRefundedAt())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
