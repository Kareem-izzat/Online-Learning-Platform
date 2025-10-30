package com.learnit.analytics;

import com.learnit.analytics.entity.EventProcessed;
import com.learnit.analytics.entity.ThreadAggregate;
import com.learnit.analytics.repository.EventProcessedRepository;
import com.learnit.analytics.repository.ThreadAggregateRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AnalyticsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ThreadAggregateRepository threadRepo;

    @Autowired
    private EventProcessedRepository processedRepo;

    @Test
    void testIngestSingleEvent_createsAggregate() throws Exception {
        // Given: a thread_created event
        String eventJson = """
                {
                  "eventType": "thread_created",
                  "eventId": "test-evt-001",
                  "occurredAt": "2025-10-30T12:00:00Z",
                  "schemaVersion": 1,
                  "sourceService": "test",
                  "payload": {
                    "threadId": 100,
                    "courseId": 50
                  }
                }
                """;

        // When: post to ingest
        mockMvc.perform(post("/api/analytics/ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson))
                .andExpect(status().isAccepted());

        // Then: aggregate exists
        Optional<ThreadAggregate> agg = threadRepo.findById(100L);
        assertTrue(agg.isPresent());
        assertEquals(50L, agg.get().getCourseId());

        // And event recorded
        assertTrue(processedRepo.existsById("test-evt-001"));
    }

    @Test
    void testIngestBatch_processesMulitpleEvents() throws Exception {
        // Given: a batch of events
        String batchJson = """
                [
                  {
                    "eventType": "thread_created",
                    "eventId": "batch-evt-001",
                    "occurredAt": "2025-10-30T12:00:00Z",
                    "schemaVersion": 1,
                    "sourceService": "test",
                    "payload": {"threadId": 200, "courseId": 60}
                  },
                  {
                    "eventType": "thread_viewed",
                    "eventId": "batch-evt-002",
                    "occurredAt": "2025-10-30T12:01:00Z",
                    "schemaVersion": 1,
                    "sourceService": "test",
                    "payload": {"threadId": 200, "viewerId": 123}
                  },
                  {
                    "eventType": "comment_added",
                    "eventId": "batch-evt-003",
                    "occurredAt": "2025-10-30T12:02:00Z",
                    "schemaVersion": 1,
                    "sourceService": "test",
                    "payload": {"threadId": 200, "commentId": 456}
                  }
                ]
                """;

        // When: post batch
        mockMvc.perform(post("/api/analytics/ingest/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(batchJson))
                .andExpect(status().isAccepted());

        // Then: aggregate has correct counts
        Optional<ThreadAggregate> agg = threadRepo.findById(200L);
        assertTrue(agg.isPresent());
        assertEquals(1, agg.get().getViews());
        assertEquals(1, agg.get().getComments());

        // All events recorded
        assertTrue(processedRepo.existsById("batch-evt-001"));
        assertTrue(processedRepo.existsById("batch-evt-002"));
        assertTrue(processedRepo.existsById("batch-evt-003"));
    }

    @Test
    void testIdempotency_duplicateEventDoesNotDoubleCount() throws Exception {
        // Given: an event
        String eventJson = """
                {
                  "eventType": "thread_viewed",
                  "eventId": "idempotent-evt-001",
                  "occurredAt": "2025-10-30T12:00:00Z",
                  "schemaVersion": 1,
                  "sourceService": "test",
                  "payload": {"threadId": 300, "viewerId": 999}
                }
                """;

        // When: post the same event twice
        mockMvc.perform(post("/api/analytics/ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson))
                .andExpect(status().isAccepted());

        mockMvc.perform(post("/api/analytics/ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson))
                .andExpect(status().isAccepted());

        // Then: views should be 1, not 2
        Optional<ThreadAggregate> agg = threadRepo.findById(300L);
        assertTrue(agg.isPresent());
        assertEquals(1, agg.get().getViews());
    }

    @Test
    void testGetThreadAggregate_returnsCorrectData() throws Exception {
        // Given: create an aggregate with some data
        String createJson = """
                {
                  "eventType": "thread_created",
                  "eventId": "get-test-001",
                  "occurredAt": "2025-10-30T12:00:00Z",
                  "schemaVersion": 1,
                  "sourceService": "test",
                  "payload": {"threadId": 400, "courseId": 70}
                }
                """;
        mockMvc.perform(post("/api/analytics/ingest")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createJson));

        // When: GET the aggregate
        mockMvc.perform(get("/api/analytics/threads/400"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.threadId").value(400))
                .andExpect(jsonPath("$.courseId").value(70))
                .andExpect(jsonPath("$.views").value(0));
    }

    @Test
    void testGetTopThreads_returnsOrderedByCourse() throws Exception {
        // Given: create multiple threads for a course with different engagement
        String thread1 = """
                {"eventType":"thread_created","eventId":"top-001","occurredAt":"2025-10-30T12:00:00Z","schemaVersion":1,"sourceService":"test","payload":{"threadId":500,"courseId":80}}
                """;
        String thread2 = """
                {"eventType":"thread_created","eventId":"top-002","occurredAt":"2025-10-30T12:00:00Z","schemaVersion":1,"sourceService":"test","payload":{"threadId":501,"courseId":80}}
                """;
        
        // Add views to thread 501 to make it "top"
        String view1 = """
                {"eventType":"thread_viewed","eventId":"top-view-001","occurredAt":"2025-10-30T12:00:00Z","schemaVersion":1,"sourceService":"test","payload":{"threadId":501,"viewerId":111}}
                """;
        String view2 = """
                {"eventType":"thread_viewed","eventId":"top-view-002","occurredAt":"2025-10-30T12:00:00Z","schemaVersion":1,"sourceService":"test","payload":{"threadId":501,"viewerId":222}}
                """;

        mockMvc.perform(post("/api/analytics/ingest").contentType(MediaType.APPLICATION_JSON).content(thread1));
        mockMvc.perform(post("/api/analytics/ingest").contentType(MediaType.APPLICATION_JSON).content(thread2));
        mockMvc.perform(post("/api/analytics/ingest").contentType(MediaType.APPLICATION_JSON).content(view1));
        mockMvc.perform(post("/api/analytics/ingest").contentType(MediaType.APPLICATION_JSON).content(view2));

        // When: get top threads
        mockMvc.perform(get("/api/analytics/courses/80/top?limit=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].threadId").value(501))  // thread 501 should be first (more views)
                .andExpect(jsonPath("$[0].views").value(2));
    }
}
