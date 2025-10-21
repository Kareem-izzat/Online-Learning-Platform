package com.learningplatform.notificationservice.service;

import com.learningplatform.notificationservice.dto.EmailRequestDto;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Send simple text email
     */
    @Async
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            log.info("Simple email sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send simple email to {}: {}", to, e.getMessage());
        }
    }

    /**
     * Send HTML email using Thymeleaf template
     */
    @Async
    public void sendTemplatedEmail(EmailRequestDto request) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(request.getTo());
            helper.setSubject(request.getSubject());

            // Process Thymeleaf template
            Context context = new Context();
            if (request.getVariables() != null) {
                request.getVariables().forEach(context::setVariable);
            }

            String htmlContent = templateEngine.process(request.getTemplate(), context);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("Templated email sent successfully to {} using template {}", request.getTo(), request.getTemplate());
        } catch (MessagingException e) {
            log.error("Failed to send templated email to {}: {}", request.getTo(), e.getMessage());
        }
    }

    /**
     * Send welcome email to new user
     */
    public void sendWelcomeEmail(String toEmail, String userName) {
        EmailRequestDto emailRequest = EmailRequestDto.builder()
                .to(toEmail)
                .subject("Welcome to Online Learning Platform!")
                .template("welcome")
                .variables(Map.of("userName", userName))
                .build();

        sendTemplatedEmail(emailRequest);
    }

    /**
     * Send enrollment confirmation email
     */
    public void sendEnrollmentEmail(String toEmail, String userName, String courseName) {
        EmailRequestDto emailRequest = EmailRequestDto.builder()
                .to(toEmail)
                .subject("Enrollment Confirmation - " + courseName)
                .template("enrollment")
                .variables(Map.of(
                        "userName", userName,
                        "courseName", courseName
                ))
                .build();

        sendTemplatedEmail(emailRequest);
    }

    /**
     * Send assignment deadline reminder
     */
    public void sendAssignmentReminderEmail(String toEmail, String userName, String assignmentTitle, String dueDate) {
        EmailRequestDto emailRequest = EmailRequestDto.builder()
                .to(toEmail)
                .subject("Assignment Reminder - " + assignmentTitle)
                .template("assignment-reminder")
                .variables(Map.of(
                        "userName", userName,
                        "assignmentTitle", assignmentTitle,
                        "dueDate", dueDate
                ))
                .build();

        sendTemplatedEmail(emailRequest);
    }

    /**
     * Send assignment graded notification
     */
    public void sendAssignmentGradedEmail(String toEmail, String userName, String assignmentTitle, Double score) {
        EmailRequestDto emailRequest = EmailRequestDto.builder()
                .to(toEmail)
                .subject("Assignment Graded - " + assignmentTitle)
                .template("assignment-graded")
                .variables(Map.of(
                        "userName", userName,
                        "assignmentTitle", assignmentTitle,
                        "score", score.toString()
                ))
                .build();

        sendTemplatedEmail(emailRequest);
    }
}
