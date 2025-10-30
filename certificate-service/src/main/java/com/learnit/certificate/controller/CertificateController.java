package com.learnit.certificate.controller;

import com.learnit.certificate.dto.*;
import com.learnit.certificate.service.CertificateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;

    @PostMapping
    public ResponseEntity<CertificateResponse> generateCertificate(@Valid @RequestBody CertificateRequest request) {
        CertificateResponse response = certificateService.generateCertificate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CertificateResponse> getCertificateById(@PathVariable Long id) {
        CertificateResponse response = certificateService.getCertificateById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/number/{certificateNumber}")
    public ResponseEntity<CertificateResponse> getCertificateByCertificateNumber(
            @PathVariable String certificateNumber) {
        CertificateResponse response = certificateService.getCertificateByCertificateNumber(certificateNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadCertificate(@PathVariable Long id) {
        Resource resource = certificateService.downloadCertificate(id);
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"certificate.pdf\"")
                .body(resource);
    }

    @GetMapping("/verify/{verificationCode}")
    public ResponseEntity<VerificationResponse> verifyCertificate(@PathVariable String verificationCode) {
        VerificationResponse response = certificateService.verifyCertificate(verificationCode);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<CertificateResponse>> getStudentCertificates(@PathVariable Long studentId) {
        List<CertificateResponse> certificates = certificateService.getStudentCertificates(studentId);
        return ResponseEntity.ok(certificates);
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<CertificateResponse>> getCourseCertificates(@PathVariable Long courseId) {
        List<CertificateResponse> certificates = certificateService.getCourseCertificates(courseId);
        return ResponseEntity.ok(certificates);
    }

    @PutMapping("/{id}/revoke")
    public ResponseEntity<CertificateResponse> revokeCertificate(
            @PathVariable Long id,
            @RequestParam String reason) {
        CertificateResponse response = certificateService.revokeCertificate(id, reason);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/student/{studentId}/stats")
    public ResponseEntity<StudentCertificateStats> getStudentStats(@PathVariable Long studentId) {
        StudentCertificateStats stats = certificateService.getStudentStats(studentId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/course/{courseId}/stats")
    public ResponseEntity<CourseCertificateStats> getCourseStats(@PathVariable Long courseId) {
        CourseCertificateStats stats = certificateService.getCourseStats(courseId);
        return ResponseEntity.ok(stats);
    }
}
