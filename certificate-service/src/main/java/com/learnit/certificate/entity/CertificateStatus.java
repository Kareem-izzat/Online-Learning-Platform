package com.learnit.certificate.entity;

public enum CertificateStatus {
    ACTIVE,      // Certificate is valid
    REVOKED,     // Certificate has been revoked
    EXPIRED,     // Certificate has expired
    SUSPENDED    // Certificate is temporarily suspended
}
