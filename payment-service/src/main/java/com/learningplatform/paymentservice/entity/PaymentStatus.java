package com.learningplatform.paymentservice.entity;

/**
 * Payment status lifecycle
 */
public enum PaymentStatus {
    PENDING,      // Payment initiated but not confirmed
    PROCESSING,   // Payment being processed by Stripe
    COMPLETED,    // Payment successful
    FAILED,       // Payment failed
    REFUNDED,     // Payment refunded
    CANCELLED     // Payment cancelled by user
}
