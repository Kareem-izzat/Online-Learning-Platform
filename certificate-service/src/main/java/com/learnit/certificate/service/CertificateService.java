package com.learnit.certificate.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.learnit.certificate.dto.*;
import com.learnit.certificate.entity.Certificate;
import com.learnit.certificate.entity.CertificateStatus;
import com.learnit.certificate.repository.CertificateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CertificateService {

    private final CertificateRepository certificateRepository;

    @Value("${certificate.storage.path}")
    private String storagePath;

    @Value("${certificate.issuer.name}")
    private String issuerName;

    @Value("${certificate.issuer.signature}")
    private String issuerSignature;

    @Value("${certificate.verification.url}")
    private String verificationUrl;

    @Transactional
    public CertificateResponse generateCertificate(CertificateRequest request) {
        // Check if certificate already exists
        boolean exists = certificateRepository.existsByStudentIdAndCourseIdAndStatus(
                request.getStudentId(), 
                request.getCourseId(), 
                CertificateStatus.ACTIVE
        );

        if (exists) {
            throw new RuntimeException("Active certificate already exists for this student and course");
        }

        // Generate unique identifiers
        String certificateNumber = generateCertificateNumber();
        String verificationCode = generateVerificationCode();

        // Create certificate entity
        Certificate certificate = Certificate.builder()
                .certificateNumber(certificateNumber)
                .verificationCode(verificationCode)
                .studentId(request.getStudentId())
                .studentName(request.getStudentName())
                .studentEmail(request.getStudentEmail())
                .courseId(request.getCourseId())
                .courseName(request.getCourseName())
                .instructorName(request.getInstructorName())
                .courseDurationHours(request.getCourseDurationHours())
                .finalGrade(request.getFinalGrade())
                .completionDate(request.getCompletionDate())
                .issueDate(LocalDateTime.now())
                .status(CertificateStatus.ACTIVE)
                .templateName(request.getTemplateName() != null ? request.getTemplateName() : "default")
                .notes(request.getNotes())
                .downloadCount(0)
                .build();

        // Generate PDF
        try {
            String pdfPath = generatePdfCertificate(certificate);
            certificate.setPdfFilePath(pdfPath);
        } catch (IOException e) {
            log.error("Failed to generate PDF for certificate: {}", certificateNumber, e);
            throw new RuntimeException("Failed to generate certificate PDF", e);
        }

        // Save certificate
        Certificate savedCertificate = certificateRepository.save(certificate);

        return mapToResponse(savedCertificate);
    }

    private String generatePdfCertificate(Certificate certificate) throws IOException {
        // Create directories if they don't exist
        Path storageDir = Paths.get(storagePath);
        if (!Files.exists(storageDir)) {
            Files.createDirectories(storageDir);
        }

        // Generate filename
        String filename = String.format("certificate_%s_%s.pdf", 
                certificate.getCertificateNumber(), 
                System.currentTimeMillis());
        Path filePath = storageDir.resolve(filename);

        // Create PDF
        PdfWriter writer = new PdfWriter(filePath.toString());
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Set page size and margins
        document.setMargins(50, 50, 50, 50);

        // Colors
        DeviceRgb primaryColor = new DeviceRgb(0, 102, 204);  // Blue
        DeviceRgb goldColor = new DeviceRgb(255, 215, 0);     // Gold

        // Certificate Border/Header
        Paragraph header = new Paragraph("CERTIFICATE OF COMPLETION")
                .setFontSize(28)
                .setBold()
                .setFontColor(primaryColor)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(header);

        // Issuer
        Paragraph issuer = new Paragraph(issuerName)
                .setFontSize(14)
                .setItalic()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(30);
        document.add(issuer);

        // Presented to
        Paragraph presentedTo = new Paragraph("This certificate is proudly presented to")
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10);
        document.add(presentedTo);

        // Student Name (prominent)
        Paragraph studentName = new Paragraph(certificate.getStudentName())
                .setFontSize(32)
                .setBold()
                .setFontColor(goldColor)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(studentName);

        // Completion text
        Paragraph completionText = new Paragraph()
                .add("For successfully completing the course\n\n")
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(completionText);

        // Course Name (prominent)
        Paragraph courseName = new Paragraph(certificate.getCourseName())
                .setFontSize(20)
                .setBold()
                .setFontColor(primaryColor)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(courseName);

        // Instructor
        Paragraph instructor = new Paragraph()
                .add("Instructed by: ")
                .add(new Text(certificate.getInstructorName()).setBold())
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10);
        document.add(instructor);

        // Details section
        StringBuilder details = new StringBuilder();
        if (certificate.getCourseDurationHours() != null) {
            details.append(String.format("Course Duration: %.1f hours\n", certificate.getCourseDurationHours()));
        }
        if (certificate.getFinalGrade() != null) {
            details.append(String.format("Final Grade: %.2f%%\n", certificate.getFinalGrade()));
        }
        details.append(String.format("Completion Date: %s\n", 
                certificate.getCompletionDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))));
        details.append(String.format("Issue Date: %s", 
                certificate.getIssueDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))));

        Paragraph detailsParagraph = new Paragraph(details.toString())
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20)
                .setMarginBottom(30);
        document.add(detailsParagraph);

        // Signature
        Paragraph signature = new Paragraph(issuerSignature)
                .setFontSize(12)
                .setItalic()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(30);
        document.add(signature);

        // Certificate Number and Verification
        Paragraph certNumber = new Paragraph()
                .add(new Text("Certificate No: ").setFontSize(9))
                .add(new Text(certificate.getCertificateNumber()).setBold().setFontSize(9))
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5);
        document.add(certNumber);

        Paragraph verification = new Paragraph()
                .add(new Text("Verification Code: ").setFontSize(9))
                .add(new Text(certificate.getVerificationCode()).setBold().setFontSize(9))
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5);
        document.add(verification);

        Paragraph verifyUrl = new Paragraph()
                .add("Verify at: ")
                .add(new Text(verificationUrl + certificate.getVerificationCode())
                        .setFontColor(ColorConstants.BLUE)
                        .setUnderline())
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(verifyUrl);

        document.close();

        log.info("PDF certificate generated: {}", filename);
        return filePath.toString();
    }

    public Resource downloadCertificate(Long certificateId) {
        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new RuntimeException("Certificate not found"));

        if (certificate.getStatus() != CertificateStatus.ACTIVE) {
            throw new RuntimeException("Certificate is not active and cannot be downloaded");
        }

        try {
            Path filePath = Paths.get(certificate.getPdfFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                // Update download count and timestamp
                certificate.setDownloadCount(certificate.getDownloadCount() + 1);
                certificate.setDownloadedAt(LocalDateTime.now());
                certificateRepository.save(certificate);

                return resource;
            } else {
                throw new RuntimeException("Certificate file not found or not readable");
            }
        } catch (IOException e) {
            log.error("Error downloading certificate: {}", certificateId, e);
            throw new RuntimeException("Error downloading certificate", e);
        }
    }

    public VerificationResponse verifyCertificate(String verificationCode) {
        Certificate certificate = certificateRepository.findByVerificationCode(verificationCode)
                .orElse(null);

        if (certificate == null) {
            return VerificationResponse.builder()
                    .valid(false)
                    .message("Invalid verification code")
                    .build();
        }

        boolean isValid = certificate.getStatus() == CertificateStatus.ACTIVE;

        return VerificationResponse.builder()
                .valid(isValid)
                .message(isValid ? "Certificate is valid" : "Certificate is " + certificate.getStatus().name().toLowerCase())
                .certificateNumber(certificate.getCertificateNumber())
                .studentName(certificate.getStudentName())
                .courseName(certificate.getCourseName())
                .instructorName(certificate.getInstructorName())
                .completionDate(certificate.getCompletionDate())
                .issueDate(certificate.getIssueDate())
                .status(certificate.getStatus())
                .finalGrade(certificate.getFinalGrade())
                .build();
    }

    public CertificateResponse getCertificateById(Long id) {
        Certificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Certificate not found"));
        return mapToResponse(certificate);
    }

    public CertificateResponse getCertificateByCertificateNumber(String certificateNumber) {
        Certificate certificate = certificateRepository.findByCertificateNumber(certificateNumber)
                .orElseThrow(() -> new RuntimeException("Certificate not found"));
        return mapToResponse(certificate);
    }

    public List<CertificateResponse> getStudentCertificates(Long studentId) {
        List<Certificate> certificates = certificateRepository.findByStudentIdOrderByIssueDateDesc(studentId);
        return certificates.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<CertificateResponse> getCourseCertificates(Long courseId) {
        List<Certificate> certificates = certificateRepository.findByCourseIdOrderByIssueDateDesc(courseId);
        return certificates.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CertificateResponse revokeCertificate(Long certificateId, String reason) {
        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new RuntimeException("Certificate not found"));

        certificate.setStatus(CertificateStatus.REVOKED);
        certificate.setNotes(certificate.getNotes() != null 
                ? certificate.getNotes() + "\nRevoked: " + reason 
                : "Revoked: " + reason);

        Certificate updated = certificateRepository.save(certificate);
        return mapToResponse(updated);
    }

    public StudentCertificateStats getStudentStats(Long studentId) {
        Long total = certificateRepository.countByStudentId(studentId);
        Long active = certificateRepository.countByStudentIdAndStatus(studentId, CertificateStatus.ACTIVE);
        Long revoked = certificateRepository.countByStudentIdAndStatus(studentId, CertificateStatus.REVOKED);
        Double avgGrade = certificateRepository.getAverageGradeByStudent(studentId);

        List<Certificate> certificates = certificateRepository.findByStudentIdOrderByIssueDateDesc(studentId);
        String studentName = certificates.isEmpty() ? "" : certificates.get(0).getStudentName();

        return StudentCertificateStats.builder()
                .studentId(studentId)
                .studentName(studentName)
                .totalCertificates(total)
                .activeCertificates(active)
                .revokedCertificates(revoked)
                .averageGrade(avgGrade)
                .build();
    }

    public CourseCertificateStats getCourseStats(Long courseId) {
        Long total = certificateRepository.countByCourseId(courseId);
        Double avgGrade = certificateRepository.getAverageGradeByCourse(courseId);
        Long downloads = certificateRepository.getTotalDownloadsByCourse(courseId);

        List<Certificate> certificates = certificateRepository.findByCourseIdOrderByIssueDateDesc(courseId);
        String courseName = certificates.isEmpty() ? "" : certificates.get(0).getCourseName();

        return CourseCertificateStats.builder()
                .courseId(courseId)
                .courseName(courseName)
                .totalCertificatesIssued(total)
                .averageFinalGrade(avgGrade)
                .totalDownloads(downloads != null ? downloads : 0L)
                .build();
    }

    private String generateCertificateNumber() {
        String prefix = "CERT";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return String.format("%s-%s-%s", prefix, timestamp, random);
    }

    private String generateVerificationCode() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

    private CertificateResponse mapToResponse(Certificate certificate) {
        return CertificateResponse.builder()
                .id(certificate.getId())
                .certificateNumber(certificate.getCertificateNumber())
                .verificationCode(certificate.getVerificationCode())
                .studentId(certificate.getStudentId())
                .studentName(certificate.getStudentName())
                .studentEmail(certificate.getStudentEmail())
                .courseId(certificate.getCourseId())
                .courseName(certificate.getCourseName())
                .instructorName(certificate.getInstructorName())
                .courseDurationHours(certificate.getCourseDurationHours())
                .finalGrade(certificate.getFinalGrade())
                .completionDate(certificate.getCompletionDate())
                .issueDate(certificate.getIssueDate())
                .status(certificate.getStatus())
                .pdfFilePath(certificate.getPdfFilePath())
                .templateName(certificate.getTemplateName())
                .downloadCount(certificate.getDownloadCount())
                .downloadedAt(certificate.getDownloadedAt())
                .notes(certificate.getNotes())
                .createdAt(certificate.getCreatedAt())
                .updatedAt(certificate.getUpdatedAt())
                .verificationUrl(verificationUrl + certificate.getVerificationCode())
                .build();
    }
}
