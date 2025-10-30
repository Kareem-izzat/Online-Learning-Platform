package com.learnit.discussion.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Outbox pattern entity for transactional event publishing.
 * Events are saved in the same transaction as domain entities,
 * then asynchronously published to Kafka by OutboxPublisher.
 */
@Entity
@Table(name = "outbox_events", indexes = {
    @Index(name = "idx_outbox_processed", columnList = "processed,createdAt"),
    @Index(name = "idx_outbox_created", columnList = "createdAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Type of domain aggregate (THREAD, COMMENT, VOTE)
     */
    @Column(nullable = false, length = 50)
    private String aggregateType;

    /**
     * ID of the domain aggregate (threadId, commentId, voteId)
     */
    @Column(nullable = false)
    private String aggregateId;

    /**
     * Event type matching analytics contract
     * (thread_created, comment_added, vote_cast, thread_viewed)
     */
    @Column(nullable = false, length = 100)
    private String eventType;

    /**
     * JSON payload matching EventEnvelope format from analytics service
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    /**
     * Whether event has been published to Kafka
     */
    @Column(nullable = false)
    private Boolean processed = false;

    /**
     * When event was created (and domain entity was saved)
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * When event was successfully published to Kafka
     */
    private LocalDateTime processedAt;

    /**
     * Number of publish attempts (for monitoring failed events)
     */
    @Column(nullable = false)
    private Integer attemptCount = 0;

    /**
     * Last error message if publish failed
     */
    @Column(columnDefinition = "TEXT")
    private String lastError;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public void markAsProcessed() {
        this.processed = true;
        this.processedAt = LocalDateTime.now();
    }

    public void incrementAttempt() {
        this.attemptCount++;
    }

    public void recordError(String errorMessage) {
        this.lastError = errorMessage;
    }
}
