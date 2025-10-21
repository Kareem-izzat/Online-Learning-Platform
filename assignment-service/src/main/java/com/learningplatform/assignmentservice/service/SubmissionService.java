package com.learningplatform.assignmentservice.service;

import com.learningplatform.assignmentservice.dto.GradeSubmissionDto;
import com.learningplatform.assignmentservice.dto.SubmissionRequestDto;
import com.learningplatform.assignmentservice.dto.SubmissionResponseDto;
import com.learningplatform.assignmentservice.entity.Assignment;
import com.learningplatform.assignmentservice.entity.Submission;
import com.learningplatform.assignmentservice.entity.SubmissionStatus;
import com.learningplatform.assignmentservice.repository.AssignmentRepository;
import com.learningplatform.assignmentservice.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;

    @Value("${submission.upload.directory:uploads/submissions}")
    private String uploadDirectory;

    @Transactional
    public SubmissionResponseDto submitAssignment(SubmissionRequestDto request, MultipartFile file) {
        log.info("Submitting assignment ID: {} for student ID: {}", 
                request.getAssignmentId(), request.getStudentId());

        // Get assignment details
        Assignment assignment = assignmentRepository.findById(request.getAssignmentId())
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        // Check if assignment is published
        if (!assignment.getPublished()) {
            throw new RuntimeException("Cannot submit - assignment is not published");
        }

        // Check for existing submissions
        List<Submission> existingSubmissions = submissionRepository
                .findByAssignmentIdAndStudentId(request.getAssignmentId(), request.getStudentId())
                .stream().toList();

        // Check attempt limits
        if (assignment.getMaxAttempts() != null && 
            existingSubmissions.size() >= assignment.getMaxAttempts()) {
            throw new RuntimeException("Maximum attempts reached for this assignment");
        }

        // Check if late
        boolean isLate = false;
        if (assignment.getDueDate() != null && LocalDateTime.now().isAfter(assignment.getDueDate())) {
            if (assignment.getAllowLateSubmission()) {
                isLate = true;
                log.info("Late submission for assignment ID: {}", assignment.getId());
            } else {
                throw new RuntimeException("Assignment is past due date and late submissions are not allowed");
            }
        }

        // Handle file upload if present
        String fileUrl = null;
        String fileName = null;
        Long fileSize = null;

        if (file != null && !file.isEmpty()) {
            try {
                Path uploadPath = Paths.get(uploadDirectory);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                String originalFilename = file.getOriginalFilename();
                String extension = originalFilename != null && originalFilename.contains(".") 
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                    : "";
                String uniqueFilename = UUID.randomUUID().toString() + extension;
                Path filePath = uploadPath.resolve(uniqueFilename);

                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                
                fileUrl = filePath.toString();
                fileName = originalFilename;
                fileSize = file.getSize();

                log.info("File uploaded: {}", filePath.toString());
            } catch (IOException e) {
                log.error("Failed to upload file", e);
                throw new RuntimeException("Failed to upload file: " + e.getMessage());
            }
        }

        // Create submission
        Submission submission = Submission.builder()
                .assignmentId(request.getAssignmentId())
                .studentId(request.getStudentId())
                .status(SubmissionStatus.SUBMITTED)
                .submissionText(request.getSubmissionText())
                .fileUrl(fileUrl)
                .fileName(fileName)
                .fileSize(fileSize)
                .attemptNumber(existingSubmissions.size() + 1)
                .isLate(isLate)
                .timeTakenMinutes(request.getTimeTakenMinutes())
                .build();

        Submission savedSubmission = submissionRepository.save(submission);
        log.info("Submission created with ID: {}", savedSubmission.getId());

        return mapToResponseDto(savedSubmission);
    }

    @Transactional
    public SubmissionResponseDto gradeSubmission(Long submissionId, GradeSubmissionDto gradeDto) {
        log.info("Grading submission ID: {}", submissionId);

        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found with ID: " + submissionId));

        // Get assignment to validate score
        Assignment assignment = assignmentRepository.findById(submission.getAssignmentId())
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        if (gradeDto.getScore() > assignment.getTotalPoints()) {
            throw new RuntimeException("Score cannot exceed total points: " + assignment.getTotalPoints());
        }

        // Apply late penalty if applicable
        Integer finalScore = gradeDto.getScore();
        if (submission.getIsLate() && assignment.getLatePenaltyPercent() != null) {
            int penalty = (gradeDto.getScore() * assignment.getLatePenaltyPercent()) / 100;
            finalScore = gradeDto.getScore() - penalty;
            log.info("Applied late penalty of {}%. Original: {}, Final: {}", 
                    assignment.getLatePenaltyPercent(), gradeDto.getScore(), finalScore);
        }

        submission.setScore(finalScore);
        submission.setFeedback(gradeDto.getFeedback());
        submission.setGradedBy(gradeDto.getGradedBy());
        submission.setStatus(SubmissionStatus.GRADED);

        Submission gradedSubmission = submissionRepository.save(submission);
        log.info("Submission graded: {}", submissionId);

        return mapToResponseDto(gradedSubmission);
    }

    public SubmissionResponseDto getSubmissionById(Long id) {
        log.info("Fetching submission with ID: {}", id);
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Submission not found with ID: " + id));
        return mapToResponseDto(submission);
    }

    public List<SubmissionResponseDto> getSubmissionsByAssignment(Long assignmentId) {
        log.info("Fetching submissions for assignment ID: {}", assignmentId);
        return submissionRepository.findByAssignmentId(assignmentId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<SubmissionResponseDto> getSubmissionsByStudent(Long studentId) {
        log.info("Fetching submissions for student ID: {}", studentId);
        return submissionRepository.findByStudentId(studentId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public SubmissionResponseDto getStudentSubmission(Long assignmentId, Long studentId) {
        log.info("Fetching submission for assignment ID: {} and student ID: {}", assignmentId, studentId);
        Submission submission = submissionRepository.findByAssignmentIdAndStudentId(assignmentId, studentId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));
        return mapToResponseDto(submission);
    }

    public List<SubmissionResponseDto> getPendingSubmissions(Long assignmentId) {
        log.info("Fetching pending submissions for assignment ID: {}", assignmentId);
        return submissionRepository.findByAssignmentIdAndStatus(assignmentId, SubmissionStatus.SUBMITTED).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public Double getAverageScore(Long assignmentId) {
        log.info("Calculating average score for assignment ID: {}", assignmentId);
        Double average = submissionRepository.getAverageScoreForAssignment(assignmentId);
        return average != null ? average : 0.0;
    }

    public byte[] getSubmissionFile(Long submissionId) {
        log.info("Fetching submission file for ID: {}", submissionId);
        
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found with ID: " + submissionId));

        if (submission.getFileUrl() == null) {
            throw new RuntimeException("No file attached to this submission");
        }

        try {
            Path filePath = Paths.get(submission.getFileUrl());
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.error("Failed to read submission file", e);
            throw new RuntimeException("Failed to read submission file: " + e.getMessage());
        }
    }

    @Transactional
    public void deleteSubmission(Long id) {
        log.info("Deleting submission ID: {}", id);

        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Submission not found with ID: " + id));

        // Delete file if exists
        if (submission.getFileUrl() != null) {
            try {
                Path filePath = Paths.get(submission.getFileUrl());
                Files.deleteIfExists(filePath);
                log.info("File deleted: {}", filePath.toString());
            } catch (IOException e) {
                log.error("Failed to delete submission file", e);
            }
        }

        submissionRepository.deleteById(id);
        log.info("Submission deleted: {}", id);
    }

    private SubmissionResponseDto mapToResponseDto(Submission submission) {
        return SubmissionResponseDto.builder()
                .id(submission.getId())
                .assignmentId(submission.getAssignmentId())
                .studentId(submission.getStudentId())
                .status(submission.getStatus())
                .submissionText(submission.getSubmissionText())
                .fileUrl(submission.getFileUrl())
                .fileName(submission.getFileName())
                .fileSize(submission.getFileSize())
                .score(submission.getScore())
                .feedback(submission.getFeedback())
                .attemptNumber(submission.getAttemptNumber())
                .submittedAt(submission.getSubmittedAt())
                .gradedAt(submission.getGradedAt())
                .gradedBy(submission.getGradedBy())
                .isLate(submission.getIsLate())
                .timeTakenMinutes(submission.getTimeTakenMinutes())
                .createdAt(submission.getCreatedAt())
                .updatedAt(submission.getUpdatedAt())
                .build();
    }
}
