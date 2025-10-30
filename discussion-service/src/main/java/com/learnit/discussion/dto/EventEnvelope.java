package com.learnit.discussion.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Event envelope matching analytics service contract.
 * See: analytics/event-contract.md
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventEnvelope {

    /**
     * Type of event: thread_created, comment_added, vote_cast, thread_viewed
     */
    private String eventType;

    /**
     * Unique event identifier (for idempotency in analytics service)
     */
    private String eventId;

    /**
     * When the event occurred (ISO-8601 format)
     */
    private LocalDateTime occurredAt;

    /**
     * Schema version for backward compatibility
     */
    private Integer schemaVersion = 1;

    /**
     * Source service name
     */
    private String sourceService = "discussion-service";

    /**
     * Event-specific data (varies by eventType)
     */
    private Map<String, Object> payload;
}
