package com.learnit.discussion.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.learnit.discussion.dto.EventEnvelope;
import com.learnit.discussion.entity.OutboxEvent;
import com.learnit.discussion.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service for creating outbox events that will be published to Kafka.
 * Must be called within the same transaction as domain entity operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxService {

    private final OutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;

    /**
     * Create outbox event for thread creation
     */
    @Transactional
    public void publishThreadCreated(Long threadId, Long courseId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("threadId", threadId);
        payload.put("courseId", courseId);

        createOutboxEvent("THREAD", threadId.toString(), "thread_created", payload);
    }

    /**
     * Create outbox event for thread view
     */
    @Transactional
    public void publishThreadViewed(Long threadId, Long courseId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("threadId", threadId);
        payload.put("courseId", courseId);

        createOutboxEvent("THREAD", threadId.toString(), "thread_viewed", payload);
    }

    /**
     * Create outbox event for comment addition
     */
    @Transactional
    public void publishCommentAdded(Long commentId, Long threadId, Long courseId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("commentId", commentId);
        payload.put("threadId", threadId);
        payload.put("courseId", courseId);

        createOutboxEvent("COMMENT", commentId.toString(), "comment_added", payload);
    }

    /**
     * Create outbox event for vote cast
     */
    @Transactional
    public void publishVoteCast(Long voteId, String targetType, Long targetId, 
                                 String voteType, Long threadId, Long courseId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("voteId", voteId);
        payload.put("targetType", targetType);
        payload.put("targetId", targetId);
        payload.put("voteType", voteType);
        payload.put("threadId", threadId);
        payload.put("courseId", courseId);

        createOutboxEvent("VOTE", voteId.toString(), "vote_cast", payload);
    }

    /**
     * Core method to create outbox event with EventEnvelope format
     */
    private void createOutboxEvent(String aggregateType, String aggregateId, 
                                   String eventType, Map<String, Object> payload) {
        try {
            // Create event envelope matching analytics contract
            EventEnvelope envelope = new EventEnvelope();
            envelope.setEventType(eventType);
            envelope.setEventId(generateEventId(eventType, aggregateId));
            envelope.setOccurredAt(LocalDateTime.now());
            envelope.setSchemaVersion(1);
            envelope.setSourceService("discussion-service");
            envelope.setPayload(payload);

            // Serialize to JSON
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            String jsonPayload = mapper.writeValueAsString(envelope);

            // Create outbox event
            OutboxEvent outboxEvent = new OutboxEvent();
            outboxEvent.setAggregateType(aggregateType);
            outboxEvent.setAggregateId(aggregateId);
            outboxEvent.setEventType(eventType);
            outboxEvent.setPayload(jsonPayload);
            outboxEvent.setProcessed(false);
            outboxEvent.setCreatedAt(LocalDateTime.now());
            outboxEvent.setAttemptCount(0);

            outboxRepository.save(outboxEvent);

            log.debug("Created outbox event: {} for {}:{}", eventType, aggregateType, aggregateId);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event payload for {}:{}", aggregateType, aggregateId, e);
            throw new RuntimeException("Failed to create outbox event", e);
        }
    }

    /**
     * Generate unique event ID for idempotency
     * Format: eventType-aggregateId-uuid
     */
    private String generateEventId(String eventType, String aggregateId) {
        return String.format("%s-%s-%s", eventType, aggregateId, UUID.randomUUID().toString().substring(0, 8));
    }

    /**
     * Get count of pending events (for monitoring)
     */
    public long getPendingEventCount() {
        return outboxRepository.countByProcessedFalse();
    }
}
