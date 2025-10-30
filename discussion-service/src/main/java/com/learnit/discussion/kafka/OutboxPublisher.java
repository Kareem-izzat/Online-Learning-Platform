package com.learnit.discussion.kafka;

import com.learnit.discussion.entity.OutboxEvent;
import com.learnit.discussion.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Background publisher that polls outbox_events table and publishes to Kafka.
 * Runs every 5 seconds to ensure events are published with minimal delay.
 * Only activates when Kafka is enabled.
 */
@Component
@ConditionalOnProperty(name = "discussion.kafka.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private final OutboxEventRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String TOPIC = "discussion.events";
    private static final int BATCH_SIZE = 100;
    private static final int MAX_RETRY_ATTEMPTS = 5;

    /**
     * Poll and publish pending events every 5 seconds
     * Fixed delay ensures we wait 5s after previous execution completes
     */
    @Scheduled(fixedDelay = 5000, initialDelay = 10000)
    public void publishPendingEvents() {
        try {
            List<OutboxEvent> pendingEvents = outboxRepository
                .findByProcessedFalseOrderByCreatedAtAsc();

            if (pendingEvents.isEmpty()) {
                log.trace("No pending outbox events to publish");
                return;
            }

            log.info("Found {} pending outbox events, publishing to Kafka...", pendingEvents.size());

            int published = 0;
            int failed = 0;

            for (OutboxEvent event : pendingEvents) {
                try {
                    publishEvent(event);
                    published++;
                } catch (Exception e) {
                    failed++;
                    log.warn("Failed to publish event {} (attempt {}): {}", 
                            event.getId(), event.getAttemptCount(), e.getMessage());
                    
                    // Don't let one failure stop the batch
                    handlePublishFailure(event, e);
                }
            }

            log.info("Published {} events, {} failed", published, failed);

            // Alert if too many failures
            if (failed > 0) {
                checkForPersistentFailures();
            }

        } catch (Exception e) {
            log.error("Error in outbox publisher loop", e);
        }
    }

    /**
     * Publish single event to Kafka and mark as processed
     */
    @Transactional
    protected void publishEvent(OutboxEvent event) {
        // Send to Kafka (synchronous for simplicity, can be async in production)
        kafkaTemplate.send(TOPIC, event.getAggregateId(), event.getPayload())
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    throw new RuntimeException("Kafka send failed", ex);
                }
            });

        // Mark as processed in database
        event.markAsProcessed();
        event.incrementAttempt();
        outboxRepository.save(event);

        log.debug("Published event {} to Kafka: {}", event.getId(), event.getEventType());
    }

    /**
     * Handle publish failure - increment attempt count and record error
     */
    @Transactional
    protected void handlePublishFailure(OutboxEvent event, Exception error) {
        event.incrementAttempt();
        event.recordError(error.getMessage());
        outboxRepository.save(event);

        // After max attempts, consider moving to dead-letter table or alerting
        if (event.getAttemptCount() >= MAX_RETRY_ATTEMPTS) {
            log.error("Event {} has failed {} times, requires manual intervention: {}", 
                     event.getId(), event.getAttemptCount(), event.getEventType());
        }
    }

    /**
     * Check for events that have failed multiple times (monitoring)
     */
    private void checkForPersistentFailures() {
        List<OutboxEvent> failedEvents = outboxRepository.findFailedEvents(MAX_RETRY_ATTEMPTS);
        
        if (!failedEvents.isEmpty()) {
            log.warn("Found {} events with {} or more failed attempts", 
                    failedEvents.size(), MAX_RETRY_ATTEMPTS);
            
            // In production: send alert to monitoring system (Prometheus, Grafana)
            // For now: log details
            failedEvents.forEach(event -> {
                log.warn("Persistently failing event: id={}, type={}, attempts={}, error={}", 
                        event.getId(), event.getEventType(), event.getAttemptCount(), event.getLastError());
            });
        }
    }

    /**
     * Cleanup old processed events (run daily)
     * Keeps last 7 days of processed events for debugging
     */
    @Scheduled(cron = "0 0 2 * * *") // 2 AM daily
    @Transactional
    public void cleanupOldProcessedEvents() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);
        List<OutboxEvent> oldEvents = outboxRepository
            .findByProcessedTrueAndProcessedAtBefore(cutoffDate);

        if (!oldEvents.isEmpty()) {
            outboxRepository.deleteAll(oldEvents);
            log.info("Cleaned up {} old processed outbox events", oldEvents.size());
        }
    }
}
