package com.learnit.certificate.dto;

import com.learnit.certificate.entity.CertificateStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationResponse {

    private boolean valid;
    private String message;
    private String certificateNumber;
    private String studentName;
    private String courseName;
    private String instructorName;
    private LocalDateTime completionDate;
    private LocalDateTime issueDate;
    private CertificateStatus status;
    private Double finalGrade;
}
