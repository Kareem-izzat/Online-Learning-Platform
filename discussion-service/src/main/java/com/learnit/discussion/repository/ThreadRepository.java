package com.learnit.discussion.repository;

import com.learnit.discussion.entity.DiscussionThread;
import com.learnit.discussion.entity.ThreadCategory;
import com.learnit.discussion.entity.ThreadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThreadRepository extends JpaRepository<DiscussionThread, Long> {

    // Find threads by course
    List<DiscussionThread> findByCourseIdAndStatusOrderByIsPinnedDescLastActivityAtDesc(
        Long courseId, ThreadStatus status);

    // Find threads by category
    List<DiscussionThread> findByCourseIdAndCategoryAndStatusOrderByLastActivityAtDesc(
        Long courseId, ThreadCategory category, ThreadStatus status);

    // Find threads by author
    List<DiscussionThread> findByAuthorIdOrderByCreatedAtDesc(Long authorId);

    // Search threads by title or content
    @Query("SELECT t FROM DiscussionThread t WHERE t.courseId = :courseId " +
           "AND t.status = :status " +
           "AND (LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(t.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<DiscussionThread> searchThreads(
        @Param("courseId") Long courseId,
        @Param("keyword") String keyword,
        @Param("status") ThreadStatus status);

    // Find threads by tag
    @Query("SELECT t FROM DiscussionThread t JOIN t.tags tag WHERE t.courseId = :courseId " +
           "AND tag = :tag AND t.status = :status ORDER BY t.lastActivityAt DESC")
    List<DiscussionThread> findByTag(
        @Param("courseId") Long courseId,
        @Param("tag") String tag,
        @Param("status") ThreadStatus status);

    // Get popular threads (most upvotes)
    @Query("SELECT t FROM DiscussionThread t WHERE t.courseId = :courseId AND t.status = :status " +
           "ORDER BY (t.upvotes - t.downvotes) DESC, t.viewCount DESC")
    List<DiscussionThread> findPopularThreads(@Param("courseId") Long courseId, @Param("status") ThreadStatus status);

    // Get unanswered questions
    @Query("SELECT t FROM DiscussionThread t WHERE t.courseId = :courseId " +
           "AND t.category = 'QUESTION' AND t.status = :status " +
           "AND t.acceptedAnswerId IS NULL ORDER BY t.createdAt DESC")
    List<DiscussionThread> findUnansweredQuestions(@Param("courseId") Long courseId, @Param("status") ThreadStatus status);

    // Count threads by course
    Long countByCourseIdAndStatus(Long courseId, ThreadStatus status);

    // Count threads by author
    Long countByAuthorId(Long authorId);
}
