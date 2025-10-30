package com.learnit.analytics.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class EventEnvelope {
    
    @NotBlank(message = "eventType is required")
    private String eventType;
    
    @NotBlank(message = "eventId is required")
    private String eventId;
    
    @NotNull(message = "occurredAt is required")
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
