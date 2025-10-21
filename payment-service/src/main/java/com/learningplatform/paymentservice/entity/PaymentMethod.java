package com.learningplatform.paymentservice.entity;

/**
 * Payment methods supported
 */
public enum PaymentMethod {
    CREDIT_CARD,   // Credit/Debit card via Stripe
    PAYPAL,        // PayPal (future)
    BANK_TRANSFER, // Bank transfer (future)
    WALLET         // Digital wallet (future)
}
