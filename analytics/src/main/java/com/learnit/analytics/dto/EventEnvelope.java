package com.learnit.analytics.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class EventEnvelope {
    private String eventType;
    private String eventId;
    private Instant occurredAt;
    private Integer schemaVersion;
    private String sourceService;
    private Map<String, Object> payload = new HashMap<>();

    @JsonCreator
    public EventEnvelope(@JsonProperty("eventType") String eventType,
                         @JsonProperty("eventId") String eventId,
                         @JsonProperty("occurredAt") Instant occurredAt,
                         @JsonProperty("schemaVersion") Integer schemaVersion,
                         @JsonProperty("sourceService") String sourceService) {
        this.eventType = eventType;
        this.eventId = eventId;
        this.occurredAt = occurredAt;
        this.schemaVersion = schemaVersion;
        this.sourceService = sourceService;
    }

    public String getEventType() {
        return eventType;
    }

    public String getEventId() {
        return eventId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public Integer getSchemaVersion() {
        return schemaVersion;
    }

    public String getSourceService() {
        return sourceService;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    @JsonAnySetter
    public void addPayload(String key, Object value) {
        this.payload.put(key, value);
    }
}
