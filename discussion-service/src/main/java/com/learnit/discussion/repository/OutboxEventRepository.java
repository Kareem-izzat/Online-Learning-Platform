package com.learnit.discussion.repository;

import com.learnit.discussion.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    /**
     * Find unprocessed events ordered by creation time (FIFO)
     * Used by OutboxPublisher to batch publish events
     */
    List<OutboxEvent> findByProcessedFalseOrderByCreatedAtAsc();

    /**
     * Find events that failed multiple times (for monitoring/alerting)
     */
    @Query("SELECT e FROM OutboxEvent e WHERE e.processed = false AND e.attemptCount >= :maxAttempts")
    List<OutboxEvent> findFailedEvents(int maxAttempts);

    /**
     * Count unprocessed events (for monitoring)
     */
    long countByProcessedFalse();

    /**
     * Find old processed events for cleanup
     */
    List<OutboxEvent> findByProcessedTrueAndProcessedAtBefore(LocalDateTime cutoffDate);
}
