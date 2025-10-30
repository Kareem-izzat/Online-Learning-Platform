package com.learnit.discussion.service;

import com.learnit.discussion.dto.*;
import com.learnit.discussion.entity.*;
import com.learnit.discussion.repository.CommentRepository;
import com.learnit.discussion.repository.ThreadRepository;
import com.learnit.discussion.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DiscussionService {

    private final ThreadRepository threadRepository;
    private final CommentRepository commentRepository;
    private final VoteRepository voteRepository;
    private final OutboxService outboxService;

    @Value("${discussion.max-tags-per-thread:5}")
    private Integer maxTagsPerThread;

    @Value("${discussion.max-comment-depth:3}")
    private Integer maxCommentDepth;

    // ==================== Thread Management ====================

    public ThreadResponse createThread(ThreadRequest request) {
        log.info("Creating thread for course: {}", request.getCourseId());

        if (request.getTags() != null && request.getTags().size() > maxTagsPerThread) {
            throw new IllegalArgumentException("Maximum " + maxTagsPerThread + " tags allowed");
        }

        DiscussionThread thread = new DiscussionThread();
        thread.setCourseId(request.getCourseId());
        thread.setAuthorId(request.getAuthorId());
        thread.setTitle(request.getTitle());
        thread.setContent(request.getContent());
        thread.setCategory(request.getCategory());
        thread.setTags(request.getTags());
        thread.setIsPinned(request.getIsPinned() != null ? request.getIsPinned() : false);
        thread.setStatus(ThreadStatus.ACTIVE);

        DiscussionThread savedThread = threadRepository.save(thread);
        log.info("Thread created with ID: {}", savedThread.getId());

        // Publish event to outbox (in same transaction)
        outboxService.publishThreadCreated(savedThread.getId(), savedThread.getCourseId());

        return new ThreadResponse(savedThread);
    }

    public ThreadResponse getThread(Long threadId) {
        DiscussionThread thread = threadRepository.findById(threadId)
            .orElseThrow(() -> new RuntimeException("Thread not found: " + threadId));

        // Increment view count
        thread.incrementViewCount();
        threadRepository.save(thread);

        // Publish thread viewed event
        outboxService.publishThreadViewed(thread.getId(), thread.getCourseId());

        return new ThreadResponse(thread);
    }

    public List<ThreadResponse> getThreadsByCourse(Long courseId) {
        List<DiscussionThread> threads = threadRepository
            .findByCourseIdAndStatusOrderByIsPinnedDescLastActivityAtDesc(courseId, ThreadStatus.ACTIVE);

        return threads.stream()
            .map(ThreadResponse::new)
            .collect(Collectors.toList());
    }

    public List<ThreadResponse> getThreadsByCategory(Long courseId, ThreadCategory category) {
        List<DiscussionThread> threads = threadRepository
            .findByCourseIdAndCategoryAndStatusOrderByLastActivityAtDesc(courseId, category, ThreadStatus.ACTIVE);

        return threads.stream()
            .map(ThreadResponse::new)
            .collect(Collectors.toList());
    }

    public List<ThreadResponse> getThreadsByAuthor(Long authorId) {
        List<DiscussionThread> threads = threadRepository.findByAuthorIdOrderByCreatedAtDesc(authorId);

        return threads.stream()
            .map(ThreadResponse::new)
            .collect(Collectors.toList());
    }

    public List<ThreadResponse> searchThreads(Long courseId, String keyword) {
        List<DiscussionThread> threads = threadRepository.searchThreads(courseId, keyword, ThreadStatus.ACTIVE);

        return threads.stream()
            .map(ThreadResponse::new)
            .collect(Collectors.toList());
    }

    public List<ThreadResponse> getThreadsByTag(Long courseId, String tag) {
        List<DiscussionThread> threads = threadRepository.findByTag(courseId, tag, ThreadStatus.ACTIVE);

        return threads.stream()
            .map(ThreadResponse::new)
            .collect(Collectors.toList());
    }

    public List<ThreadResponse> getPopularThreads(Long courseId) {
        List<DiscussionThread> threads = threadRepository.findPopularThreads(courseId, ThreadStatus.ACTIVE);

        return threads.stream()
            .map(ThreadResponse::new)
            .collect(Collectors.toList());
    }

    public List<ThreadResponse> getUnansweredQuestions(Long courseId) {
        List<DiscussionThread> threads = threadRepository.findUnansweredQuestions(courseId, ThreadStatus.ACTIVE);

        return threads.stream()
            .map(ThreadResponse::new)
            .collect(Collectors.toList());
    }

    public ThreadResponse updateThread(Long threadId, ThreadRequest request) {
        DiscussionThread thread = threadRepository.findById(threadId)
            .orElseThrow(() -> new RuntimeException("Thread not found: " + threadId));

        // Only author can update
        if (!thread.getAuthorId().equals(request.getAuthorId())) {
            throw new RuntimeException("Only thread author can update");
        }

        thread.setTitle(request.getTitle());
        thread.setContent(request.getContent());
        thread.setCategory(request.getCategory());
        thread.setTags(request.getTags());

        DiscussionThread updatedThread = threadRepository.save(thread);
        log.info("DiscussionThread updated: {}", threadId);

        return new ThreadResponse(updatedThread);
    }

    public void deleteThread(Long threadId, Long userId) {
        DiscussionThread thread = threadRepository.findById(threadId)
            .orElseThrow(() -> new RuntimeException("Thread not found: " + threadId));

        // Only author can delete
        if (!thread.getAuthorId().equals(userId)) {
            throw new RuntimeException("Only thread author can delete");
        }

        thread.setStatus(ThreadStatus.DELETED);
        threadRepository.save(thread);
        log.info("Thread deleted: {}", threadId);
    }

    public ThreadResponse pinThread(Long threadId, Boolean isPinned) {
        DiscussionThread thread = threadRepository.findById(threadId)
            .orElseThrow(() -> new RuntimeException("Thread not found: " + threadId));

        thread.setIsPinned(isPinned);
        DiscussionThread updatedThread = threadRepository.save(thread);
        log.info("Thread {} pinned status: {}", threadId, isPinned);

        return new ThreadResponse(updatedThread);
    }

    public ThreadResponse lockThread(Long threadId, Boolean isLocked) {
        DiscussionThread thread = threadRepository.findById(threadId)
            .orElseThrow(() -> new RuntimeException("Thread not found: " + threadId));

        thread.setIsLocked(isLocked);
        DiscussionThread updatedThread = threadRepository.save(thread);
        log.info("Thread {} locked status: {}", threadId, isLocked);

        return new ThreadResponse(updatedThread);
    }

    // ==================== Comment Management ====================

    public CommentResponse addComment(CommentRequest request) {
        log.info("Adding comment to thread: {}", request.getThreadId());

        DiscussionThread thread = threadRepository.findById(request.getThreadId())
            .orElseThrow(() -> new RuntimeException("Thread not found: " + request.getThreadId()));

        if (thread.getIsLocked()) {
            throw new RuntimeException("Thread is locked, cannot add comments");
        }

        Comment comment = new Comment();
        comment.setThreadId(request.getThreadId());
        comment.setAuthorId(request.getAuthorId());
        comment.setContent(request.getContent());
        comment.setParentCommentId(request.getParentCommentId());

        // Calculate depth for nested replies
        if (request.getParentCommentId() != null) {
            Comment parentComment = commentRepository.findById(request.getParentCommentId())
                .orElseThrow(() -> new RuntimeException("Parent comment not found"));

            int newDepth = parentComment.getDepth() + 1;
            if (newDepth > maxCommentDepth) {
                throw new RuntimeException("Maximum comment depth exceeded");
            }
            comment.setDepth(newDepth);
        } else {
            comment.setDepth(0);
        }

        Comment savedComment = commentRepository.save(comment);

        // Update thread reply count and last activity
        thread.incrementReplyCount();
        thread.updateLastActivity();
        threadRepository.save(thread);

        // Publish comment added event
        outboxService.publishCommentAdded(savedComment.getId(), thread.getId(), thread.getCourseId());

        log.info("Comment added with ID: {}", savedComment.getId());
        return new CommentResponse(savedComment);
    }

    public List<CommentResponse> getThreadComments(Long threadId) {
        List<Comment> comments = commentRepository.findByThreadIdOrderByCreatedAtAsc(threadId);

        return comments.stream()
            .map(CommentResponse::new)
            .collect(Collectors.toList());
    }

    public List<CommentResponse> getTopLevelComments(Long threadId) {
        List<Comment> comments = commentRepository
            .findByThreadIdAndParentCommentIdIsNullOrderByCreatedAtAsc(threadId);

        return comments.stream()
            .map(CommentResponse::new)
            .collect(Collectors.toList());
    }

    public List<CommentResponse> getCommentReplies(Long commentId) {
        List<Comment> replies = commentRepository.findByParentCommentIdOrderByCreatedAtAsc(commentId);

        return replies.stream()
            .map(CommentResponse::new)
            .collect(Collectors.toList());
    }

    public CommentResponse updateComment(Long commentId, String content, Long userId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new RuntimeException("Comment not found: " + commentId));

        // Only author can update
        if (!comment.getAuthorId().equals(userId)) {
            throw new RuntimeException("Only comment author can update");
        }

        comment.setContent(content);
        Comment updatedComment = commentRepository.save(comment);
        log.info("Comment updated: {}", commentId);

        return new CommentResponse(updatedComment);
    }

    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new RuntimeException("Comment not found: " + commentId));

        // Only author can delete
        if (!comment.getAuthorId().equals(userId)) {
            throw new RuntimeException("Only comment author can delete");
        }

        // Update thread reply count
        DiscussionThread thread = threadRepository.findById(comment.getThreadId())
            .orElseThrow(() -> new RuntimeException("Thread not found"));
        thread.decrementReplyCount();
        threadRepository.save(thread);

        commentRepository.delete(comment);
        log.info("Comment deleted: {}", commentId);
    }

    public CommentResponse markAsAnswer(Long threadId, Long commentId) {
        DiscussionThread thread = threadRepository.findById(threadId)
            .orElseThrow(() -> new RuntimeException("Thread not found: " + threadId));

        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new RuntimeException("Comment not found: " + commentId));

        if (!comment.getThreadId().equals(threadId)) {
            throw new RuntimeException("Comment does not belong to this thread");
        }

        // Unmark previous answer if exists
        commentRepository.findByThreadIdAndIsAnswerTrue(threadId)
            .ifPresent(prevAnswer -> {
                prevAnswer.unmarkAsAnswer();
                commentRepository.save(prevAnswer);
            });

        // Mark new answer
        comment.markAsAnswer();
        Comment savedComment = commentRepository.save(comment);

        // Update thread with accepted answer
        thread.setAcceptedAnswerId(commentId);
        threadRepository.save(thread);

        log.info("Comment {} marked as answer for thread {}", commentId, threadId);
        return new CommentResponse(savedComment);
    }

    // ==================== Voting System ====================

    public VoteResponse castVote(VoteRequest request) {
        log.info("User {} casting {} on {} {}", 
            request.getUserId(), request.getVoteType(), request.getTargetType(), request.getTargetId());

        // Check if user already voted
        voteRepository.findByUserIdAndTargetTypeAndTargetId(
            request.getUserId(), request.getTargetType(), request.getTargetId()
        ).ifPresent(existingVote -> {
            // Remove old vote counts
            updateVoteCount(existingVote.getTargetType(), existingVote.getTargetId(), 
                           existingVote.getVoteType(), false);
            voteRepository.delete(existingVote);
        });

        // Create new vote
        Vote vote = new Vote();
        vote.setUserId(request.getUserId());
        vote.setTargetType(request.getTargetType());
        vote.setTargetId(request.getTargetId());
        vote.setVoteType(request.getVoteType());

        Vote savedVote = voteRepository.save(vote);

        // Update vote count
        updateVoteCount(request.getTargetType(), request.getTargetId(), request.getVoteType(), true);

        // Get thread and course info for event
        Long threadId = getThreadIdForTarget(request.getTargetType(), request.getTargetId());
        Long courseId = getCourseIdForThread(threadId);

        // Publish vote cast event
        outboxService.publishVoteCast(
            savedVote.getId(),
            request.getTargetType().name(),
            request.getTargetId(),
            request.getVoteType().name(),
            threadId,
            courseId
        );

        log.info("Vote cast successfully");
        return new VoteResponse(savedVote);
    }

    public void removeVote(Long userId, TargetType targetType, Long targetId) {
        Vote vote = voteRepository.findByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId)
            .orElseThrow(() -> new RuntimeException("Vote not found"));

        // Update vote count
        updateVoteCount(targetType, targetId, vote.getVoteType(), false);

        voteRepository.delete(vote);
        log.info("Vote removed for user {} on {} {}", userId, targetType, targetId);
    }

    public VoteResponse getUserVote(Long userId, TargetType targetType, Long targetId) {
        Vote vote = voteRepository.findByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId)
            .orElseThrow(() -> new RuntimeException("User has not voted on this target"));

        return new VoteResponse(vote);
    }

    private void updateVoteCount(TargetType targetType, Long targetId, VoteType voteType, boolean increment) {
        if (targetType == TargetType.THREAD) {
            DiscussionThread thread = threadRepository.findById(targetId)
                .orElseThrow(() -> new RuntimeException("Thread not found"));

            if (voteType == VoteType.UPVOTE) {
                if (increment) {
                    thread.addUpvote();
                } else {
                    thread.removeUpvote();
                }
            } else {
                if (increment) {
                    thread.addDownvote();
                } else {
                    thread.removeDownvote();
                }
            }
            threadRepository.save(thread);
        } else if (targetType == TargetType.COMMENT) {
            Comment comment = commentRepository.findById(targetId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

            if (voteType == VoteType.UPVOTE) {
                if (increment) {
                    comment.addUpvote();
                } else {
                    comment.removeUpvote();
                }
            } else {
                if (increment) {
                    comment.addDownvote();
                } else {
                    comment.removeDownvote();
                }
            }
            commentRepository.save(comment);
        }
    }

    // ==================== Statistics ====================

    public ThreadStatistics getThreadStatistics(Long threadId) {
        DiscussionThread thread = threadRepository.findById(threadId)
            .orElseThrow(() -> new RuntimeException("Thread not found: " + threadId));

        ThreadStatistics stats = new ThreadStatistics();
        stats.setThreadId(threadId);
        stats.setTotalViews(thread.getViewCount());
        stats.setTotalReplies(thread.getReplyCount());
        stats.setTotalUpvotes(thread.getUpvotes());
        stats.setTotalDownvotes(thread.getDownvotes());
        stats.setNetVotes(thread.getUpvotes() - thread.getDownvotes());
        stats.setHasAcceptedAnswer(thread.getAcceptedAnswerId() != null);

        // Calculate engagement score: views + (replies * 2) + (netVotes * 5)
        double engagementScore = thread.getViewCount() + 
                                (thread.getReplyCount() * 2.0) + 
                                ((thread.getUpvotes() - thread.getDownvotes()) * 5.0);
        stats.setEngagementScore(engagementScore);

        return stats;
    }

    // ==================== Helper Methods for Events ====================

    private Long getThreadIdForTarget(TargetType targetType, Long targetId) {
        if (targetType == TargetType.THREAD) {
            return targetId;
        } else if (targetType == TargetType.COMMENT) {
            Comment comment = commentRepository.findById(targetId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
            return comment.getThreadId();
        }
        throw new IllegalArgumentException("Unknown target type: " + targetType);
    }

    private Long getCourseIdForThread(Long threadId) {
        DiscussionThread thread = threadRepository.findById(threadId)
            .orElseThrow(() -> new RuntimeException("Thread not found"));
        return thread.getCourseId();
    }
}
