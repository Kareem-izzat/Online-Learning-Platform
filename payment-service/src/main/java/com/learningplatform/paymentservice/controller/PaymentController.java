package com.learningplatform.paymentservice.controller;

import com.learningplatform.paymentservice.dto.PaymentRequestDto;
import com.learningplatform.paymentservice.dto.PaymentResponseDto;
import com.learningplatform.paymentservice.dto.RefundRequestDto;
import com.learningplatform.paymentservice.service.PaymentService;

import java.math.BigDecimal;
import com.stripe.exception.StripeException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponseDto> createPayment(
            @Valid @RequestBody PaymentRequestDto request) throws StripeException {
        PaymentResponseDto payment = paymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    @PutMapping("/confirm/{paymentIntentId}")
    public ResponseEntity<PaymentResponseDto> confirmPayment(
            @PathVariable String paymentIntentId) {
        PaymentResponseDto payment = paymentService.confirmPayment(paymentIntentId);
        return ResponseEntity.ok(payment);
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<PaymentResponseDto> refundPayment(
            @PathVariable Long id,
            @Valid @RequestBody RefundRequestDto request) throws StripeException {
        PaymentResponseDto payment = paymentService.refundPayment(id, request);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponseDto> getPayment(@PathVariable Long id) {
        PaymentResponseDto payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentResponseDto>> getUserPayments(@PathVariable Long userId) {
        List<PaymentResponseDto> payments = paymentService.getUserPayments(userId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/revenue/total")
    public ResponseEntity<BigDecimal> getTotalRevenue() {
        BigDecimal revenue = paymentService.getTotalRevenue();
        return ResponseEntity.ok(revenue);
    }

    @GetMapping("/revenue/course/{courseId}")
    public ResponseEntity<BigDecimal> getRevenueByCourse(@PathVariable Long courseId) {
        BigDecimal revenue = paymentService.getRevenueByCourse(courseId);
        return ResponseEntity.ok(revenue);
    }
}
