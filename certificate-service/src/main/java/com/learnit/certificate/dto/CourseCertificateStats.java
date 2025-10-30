package com.learnit.certificate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseCertificateStats {

    private Long courseId;
    private String courseName;
    private Long totalCertificatesIssued;
    private Double averageFinalGrade;
    private Long totalDownloads;
}
