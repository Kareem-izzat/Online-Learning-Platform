package com.learnit.discussion.repository;

import com.learnit.discussion.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Find all comments for a thread (ordered by votes)
    List<Comment> findByThreadIdOrderByCreatedAtAsc(Long threadId);

    // Find top-level comments (no parent)
    List<Comment> findByThreadIdAndParentCommentIdIsNullOrderByCreatedAtAsc(Long threadId);

    // Find replies to a comment
    List<Comment> findByParentCommentIdOrderByCreatedAtAsc(Long parentCommentId);

    // Find comments by author
    List<Comment> findByAuthorIdOrderByCreatedAtDesc(Long authorId);

    // Get best answer for a thread
    Optional<Comment> findByThreadIdAndIsAnswerTrue(Long threadId);

    // Count comments in a thread
    Long countByThreadId(Long threadId);

    // Count comments by author
    Long countByAuthorId(Long authorId);

    // Get top comments by votes
    @Query("SELECT c FROM Comment c WHERE c.threadId = :threadId " +
           "ORDER BY (c.upvotes - c.downvotes) DESC, c.createdAt ASC")
    List<Comment> findTopCommentsByVotes(@Param("threadId") Long threadId);

    // Check if comment exists with depth limit
    @Query("SELECT COUNT(c) > 0 FROM Comment c WHERE c.id = :commentId AND c.depth < :maxDepth")
    boolean canAddReply(@Param("commentId") Long commentId, @Param("maxDepth") Integer maxDepth);
}
