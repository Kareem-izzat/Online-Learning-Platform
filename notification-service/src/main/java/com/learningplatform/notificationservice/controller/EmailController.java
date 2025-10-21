package com.learningplatform.notificationservice.controller;

import com.learningplatform.notificationservice.dto.EmailRequestDto;
import com.learningplatform.notificationservice.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    /**
     * Send simple text email
     */
    @PostMapping("/simple")
    public ResponseEntity<String> sendSimpleEmail(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String text) {
        emailService.sendSimpleEmail(to, subject, text);
        return ResponseEntity.ok("Email sent successfully");
    }

    /**
     * Send templated email
     */
    @PostMapping("/templated")
    public ResponseEntity<String> sendTemplatedEmail(@Valid @RequestBody EmailRequestDto request) {
        emailService.sendTemplatedEmail(request);
        return ResponseEntity.ok("Templated email sent successfully");
    }

    /**
     * Send welcome email
     */
    @PostMapping("/welcome")
    public ResponseEntity<String> sendWelcomeEmail(
            @RequestParam String toEmail,
            @RequestParam String userName) {
        emailService.sendWelcomeEmail(toEmail, userName);
        return ResponseEntity.ok("Welcome email sent successfully");
    }

    /**
     * Send enrollment confirmation email
     */
    @PostMapping("/enrollment")
    public ResponseEntity<String> sendEnrollmentEmail(
            @RequestParam String toEmail,
            @RequestParam String userName,
            @RequestParam String courseName) {
        emailService.sendEnrollmentEmail(toEmail, userName, courseName);
        return ResponseEntity.ok("Enrollment email sent successfully");
    }

    /**
     * Send assignment reminder email
     */
    @PostMapping("/assignment-reminder")
    public ResponseEntity<String> sendAssignmentReminderEmail(
            @RequestParam String toEmail,
            @RequestParam String userName,
            @RequestParam String assignmentTitle,
            @RequestParam String dueDate) {
        emailService.sendAssignmentReminderEmail(toEmail, userName, assignmentTitle, dueDate);
        return ResponseEntity.ok("Assignment reminder email sent successfully");
    }

    /**
     * Send assignment graded email
     */
    @PostMapping("/assignment-graded")
    public ResponseEntity<String> sendAssignmentGradedEmail(
            @RequestParam String toEmail,
            @RequestParam String userName,
            @RequestParam String assignmentTitle,
            @RequestParam Double score) {
        emailService.sendAssignmentGradedEmail(toEmail, userName, assignmentTitle, score);
        return ResponseEntity.ok("Assignment graded email sent successfully");
    }
}
