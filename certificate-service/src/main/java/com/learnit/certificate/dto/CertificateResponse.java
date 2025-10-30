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
public class CertificateResponse {

    private Long id;
    private String certificateNumber;
    private String verificationCode;
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private Long courseId;
    private String courseName;
    private String instructorName;
    private Double courseDurationHours;
    private Double finalGrade;
    private LocalDateTime completionDate;
    private LocalDateTime issueDate;
    private CertificateStatus status;
    private String pdfFilePath;
    private String templateName;
    private Integer downloadCount;
    private LocalDateTime downloadedAt;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String verificationUrl;
}
