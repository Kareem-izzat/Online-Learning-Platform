package com.learnit.analytics.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnit.analytics.dto.EventEnvelope;
import com.learnit.analytics.service.AnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "analytics.kafka.enabled", havingValue = "true")
public class AnalyticsKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsKafkaConsumer.class);

    private final AnalyticsService analyticsService;
    private final ObjectMapper objectMapper;

    public AnalyticsKafkaConsumer(AnalyticsService analyticsService, ObjectMapper objectMapper) {
        this.analyticsService = analyticsService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = {"${analytics.kafka.topic.discussion:discussion.events}"}, groupId = "${analytics.kafka.group-id:analytics-service}")
    public void consumeEvent(String message) {
        try {
            log.info("Received Kafka message: {}", message);
            EventEnvelope envelope = objectMapper.readValue(message, EventEnvelope.class);
            analyticsService.processEvent(envelope);
            log.info("Successfully processed event: {}", envelope.getEventId());
        } catch (Exception e) {
            log.error("Error processing Kafka message: {}", message, e);
            // In production: send to dead-letter queue or retry topic
        }
    }
}
