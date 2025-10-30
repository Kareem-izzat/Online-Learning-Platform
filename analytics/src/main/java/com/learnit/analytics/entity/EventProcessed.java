package com.learnit.analytics.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "event_processed")
public class EventProcessed {
    @Id
    @Column(name = "event_id", nullable = false)
    private String eventId;

    @Column(name = "event_type")
    private String eventType;

    @Column(name = "received_at")
    private Instant receivedAt;

    public EventProcessed() {}

    public EventProcessed(String eventId, String eventType, Instant receivedAt) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.receivedAt = receivedAt;
    }

    public String getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }
}
