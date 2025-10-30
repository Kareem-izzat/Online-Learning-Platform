package com.learnit.discussion.controller;

import com.learnit.discussion.dto.*;
import com.learnit.discussion.entity.TargetType;
import com.learnit.discussion.entity.ThreadCategory;
import com.learnit.discussion.service.DiscussionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/discussions")
@RequiredArgsConstructor
public class DiscussionController {

    private final DiscussionService discussionService;

    // ==================== Thread Management ====================

    @PostMapping("/threads")
    public ResponseEntity<ThreadResponse> createThread(@Valid @RequestBody ThreadRequest request) {
        ThreadResponse response = discussionService.createThread(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/threads/{id}")
    public ResponseEntity<ThreadResponse> getThread(@PathVariable Long id) {
        ThreadResponse response = discussionService.getThread(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/threads/course/{courseId}")
    public ResponseEntity<List<ThreadResponse>> getThreadsByCourse(@PathVariable Long courseId) {
        List<ThreadResponse> response = discussionService.getThreadsByCourse(courseId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/threads/course/{courseId}/category/{category}")
    public ResponseEntity<List<ThreadResponse>> getThreadsByCategory(
            @PathVariable Long courseId,
            @PathVariable ThreadCategory category) {
        List<ThreadResponse> response = discussionService.getThreadsByCategory(courseId, category);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/threads/user/{userId}")
    public ResponseEntity<List<ThreadResponse>> getThreadsByAuthor(@PathVariable Long userId) {
        List<ThreadResponse> response = discussionService.getThreadsByAuthor(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/threads/course/{courseId}/search")
    public ResponseEntity<List<ThreadResponse>> searchThreads(
            @PathVariable Long courseId,
            @RequestParam String keyword) {
        List<ThreadResponse> response = discussionService.searchThreads(courseId, keyword);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/threads/course/{courseId}/tag/{tag}")
    public ResponseEntity<List<ThreadResponse>> getThreadsByTag(
            @PathVariable Long courseId,
            @PathVariable String tag) {
        List<ThreadResponse> response = discussionService.getThreadsByTag(courseId, tag);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/threads/course/{courseId}/popular")
    public ResponseEntity<List<ThreadResponse>> getPopularThreads(@PathVariable Long courseId) {
        List<ThreadResponse> response = discussionService.getPopularThreads(courseId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/threads/course/{courseId}/unanswered")
    public ResponseEntity<List<ThreadResponse>> getUnansweredQuestions(@PathVariable Long courseId) {
        List<ThreadResponse> response = discussionService.getUnansweredQuestions(courseId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/threads/{id}")
    public ResponseEntity<ThreadResponse> updateThread(
            @PathVariable Long id,
            @Valid @RequestBody ThreadRequest request) {
        ThreadResponse response = discussionService.updateThread(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/threads/{id}")
    public ResponseEntity<Void> deleteThread(
            @PathVariable Long id,
            @RequestParam Long userId) {
        discussionService.deleteThread(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/threads/{id}/pin")
    public ResponseEntity<ThreadResponse> pinThread(
            @PathVariable Long id,
            @RequestParam Boolean isPinned) {
        ThreadResponse response = discussionService.pinThread(id, isPinned);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/threads/{id}/lock")
    public ResponseEntity<ThreadResponse> lockThread(
            @PathVariable Long id,
            @RequestParam Boolean isLocked) {
        ThreadResponse response = discussionService.lockThread(id, isLocked);
        return ResponseEntity.ok(response);
    }

    // ==================== Comment Management ====================

    @PostMapping("/comments")
    public ResponseEntity<CommentResponse> addComment(@Valid @RequestBody CommentRequest request) {
        CommentResponse response = discussionService.addComment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/comments/thread/{threadId}")
    public ResponseEntity<List<CommentResponse>> getThreadComments(@PathVariable Long threadId) {
        List<CommentResponse> response = discussionService.getThreadComments(threadId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/comments/thread/{threadId}/top-level")
    public ResponseEntity<List<CommentResponse>> getTopLevelComments(@PathVariable Long threadId) {
        List<CommentResponse> response = discussionService.getTopLevelComments(threadId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/comments/{commentId}/replies")
    public ResponseEntity<List<CommentResponse>> getCommentReplies(@PathVariable Long commentId) {
        List<CommentResponse> response = discussionService.getCommentReplies(commentId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/comments/{id}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long id,
            @RequestParam String content,
            @RequestParam Long userId) {
        CommentResponse response = discussionService.updateComment(id, content, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long id,
            @RequestParam Long userId) {
        discussionService.deleteComment(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/threads/{threadId}/comments/{commentId}/mark-answer")
    public ResponseEntity<CommentResponse> markAsAnswer(
            @PathVariable Long threadId,
            @PathVariable Long commentId) {
        CommentResponse response = discussionService.markAsAnswer(threadId, commentId);
        return ResponseEntity.ok(response);
    }

    // ==================== Voting System ====================

    @PostMapping("/votes")
    public ResponseEntity<VoteResponse> castVote(@Valid @RequestBody VoteRequest request) {
        VoteResponse response = discussionService.castVote(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/votes")
    public ResponseEntity<Void> removeVote(
            @RequestParam Long userId,
            @RequestParam TargetType targetType,
            @RequestParam Long targetId) {
        discussionService.removeVote(userId, targetType, targetId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/votes")
    public ResponseEntity<VoteResponse> getUserVote(
            @RequestParam Long userId,
            @RequestParam TargetType targetType,
            @RequestParam Long targetId) {
        VoteResponse response = discussionService.getUserVote(userId, targetType, targetId);
        return ResponseEntity.ok(response);
    }

    // ==================== Statistics ====================

    @GetMapping("/threads/{id}/stats")
    public ResponseEntity<ThreadStatistics> getThreadStatistics(@PathVariable Long id) {
        ThreadStatistics stats = discussionService.getThreadStatistics(id);
        return ResponseEntity.ok(stats);
    }

    // ==================== Health Check ====================

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Discussion Service is running");
    }
}
