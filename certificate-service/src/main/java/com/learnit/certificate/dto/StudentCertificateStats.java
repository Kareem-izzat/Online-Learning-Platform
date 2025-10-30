package com.learnit.certificate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentCertificateStats {

    private Long studentId;
    private String studentName;
    private Long totalCertificates;
    private Long activeCertificates;
    private Long revokedCertificates;
    private Double averageGrade;
}
