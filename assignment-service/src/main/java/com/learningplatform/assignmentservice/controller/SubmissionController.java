package com.learningplatform.assignmentservice.controller;

import com.learningplatform.assignmentservice.dto.GradeSubmissionDto;
import com.learningplatform.assignmentservice.dto.SubmissionRequestDto;
import com.learningplatform.assignmentservice.dto.SubmissionResponseDto;
import com.learningplatform.assignmentservice.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
@Slf4j
public class SubmissionController {

    private final SubmissionService submissionService;

    // Submit assignment (with optional file)
    @PostMapping(value = "/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SubmissionResponseDto> submitAssignment(
            @RequestParam("assignmentId") Long assignmentId,
            @RequestParam("studentId") Long studentId,
            @RequestParam(value = "submissionText", required = false) String submissionText,
            @RequestParam(value = "attemptNumber", required = false) Integer attemptNumber,
            @RequestParam(value = "timeTakenMinutes", required = false) Integer timeTakenMinutes,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        log.info("Submit assignment request received for assignment ID: {} by student ID: {}", 
                assignmentId, studentId);

        SubmissionRequestDto request = SubmissionRequestDto.builder()
                .assignmentId(assignmentId)
                .studentId(studentId)
                .submissionText(submissionText)
                .attemptNumber(attemptNumber)
                .timeTakenMinutes(timeTakenMinutes)
                .build();

        SubmissionResponseDto response = submissionService.submitAssignment(request, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Grade a submission
    @PutMapping("/{id}/grade")
    public ResponseEntity<SubmissionResponseDto> gradeSubmission(
            @PathVariable Long id,
            @Valid @RequestBody GradeSubmissionDto gradeDto) {
        log.info("Grade submission request for ID: {}", id);
        SubmissionResponseDto gradedSubmission = submissionService.gradeSubmission(id, gradeDto);
        return ResponseEntity.ok(gradedSubmission);
    }

    // Get all submissions for an assignment
    @GetMapping("/assignment/{assignmentId}")
    public ResponseEntity<List<SubmissionResponseDto>> getSubmissionsByAssignment(@PathVariable Long assignmentId) {
        log.info("Get submissions for assignment ID: {}", assignmentId);
        List<SubmissionResponseDto> submissions = submissionService.getSubmissionsByAssignment(assignmentId);
        return ResponseEntity.ok(submissions);
    }

    // Get all submissions by a student
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<SubmissionResponseDto>> getSubmissionsByStudent(@PathVariable Long studentId) {
        log.info("Get submissions for student ID: {}", studentId);
        List<SubmissionResponseDto> submissions = submissionService.getSubmissionsByStudent(studentId);
        return ResponseEntity.ok(submissions);
    }

    // Get specific student's submission for an assignment
    @GetMapping("/assignment/{assignmentId}/student/{studentId}")
    public ResponseEntity<SubmissionResponseDto> getStudentSubmission(
            @PathVariable Long assignmentId,
            @PathVariable Long studentId) {
        log.info("Get submission for assignment ID: {} and student ID: {}", assignmentId, studentId);
        SubmissionResponseDto submission = submissionService.getStudentSubmission(assignmentId, studentId);
        return ResponseEntity.ok(submission);
    }

    // Get pending submissions for an assignment
    @GetMapping("/assignment/{assignmentId}/pending")
    public ResponseEntity<List<SubmissionResponseDto>> getPendingSubmissions(@PathVariable Long assignmentId) {
        log.info("Get pending submissions for assignment ID: {}", assignmentId);
        List<SubmissionResponseDto> submissions = submissionService.getPendingSubmissions(assignmentId);
        return ResponseEntity.ok(submissions);
    }

    // Get average score for an assignment
    @GetMapping("/assignment/{assignmentId}/average-score")
    public ResponseEntity<Double> getAverageScore(@PathVariable Long assignmentId) {
        log.info("Get average score for assignment ID: {}", assignmentId);
        Double averageScore = submissionService.getAverageScore(assignmentId);
        return ResponseEntity.ok(averageScore);
    }

    // Download submission file
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadSubmissionFile(@PathVariable Long id) {
        log.info("Download submission file for ID: {}", id);
        
        byte[] fileData = submissionService.getSubmissionFile(id);
        SubmissionResponseDto submission = submissionService.getSubmissionById(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentLength(fileData.length);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, 
            "attachment; filename=\"" + submission.getFileName() + "\"");

        return new ResponseEntity<>(fileData, headers, HttpStatus.OK);
    }

    // Get submission by ID
    @GetMapping("/{id}")
    public ResponseEntity<SubmissionResponseDto> getSubmissionById(@PathVariable Long id) {
        log.info("Get submission request for ID: {}", id);
        SubmissionResponseDto submission = submissionService.getSubmissionById(id);
        return ResponseEntity.ok(submission);
    }

    // Delete submission
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubmission(@PathVariable Long id) {
        log.info("Delete submission request for ID: {}", id);
        submissionService.deleteSubmission(id);
        return ResponseEntity.noContent().build();
    }
}
