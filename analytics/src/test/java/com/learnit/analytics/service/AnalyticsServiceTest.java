package com.learnit.analytics.service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.learnit.analytics.dto.EventEnvelope;
import com.learnit.analytics.entity.EventProcessed;
import com.learnit.analytics.entity.ThreadAggregate;
import com.learnit.analytics.repository.EventProcessedRepository;
import com.learnit.analytics.repository.ThreadAggregateRepository;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private ThreadAggregateRepository threadRepo;

    @Mock
    private EventProcessedRepository processedRepo;

    private AnalyticsService service;

    @BeforeEach
    void setUp() {
        service = new AnalyticsService(threadRepo, processedRepo);
    }

    @Test
    void testIdempotency_duplicateEventIsIgnored() {
        // Given: an event that was already processed
        String eventId = "test-event-123";
        when(processedRepo.existsById(eventId)).thenReturn(true);

        EventEnvelope event = createEvent(eventId, "thread_created", Map.of("threadId", 123, "courseId", 42));

        // When: process the event
        service.processEvent(event);

        // Then: no aggregate operations should happen
        verify(threadRepo, never()).save(any());
        verify(processedRepo, never()).save(any());
    }

    @Test
    void testIdempotency_newEventIsProcessedAndRecorded() {
        // Given: a new event
        String eventId = "new-event-456";
        when(processedRepo.existsById(eventId)).thenReturn(false);
        when(threadRepo.existsById(123L)).thenReturn(false);

        EventEnvelope event = createEvent(eventId, "thread_created", Map.of("threadId", 123, "courseId", 42));

        // When: process the event
        service.processEvent(event);

        // Then: aggregate should be saved and event recorded
        ArgumentCaptor<ThreadAggregate> aggCaptor = ArgumentCaptor.forClass(ThreadAggregate.class);
        verify(threadRepo).save(aggCaptor.capture());
        assertEquals(123L, aggCaptor.getValue().getThreadId());

        ArgumentCaptor<EventProcessed> processedCaptor = ArgumentCaptor.forClass(EventProcessed.class);
        verify(processedRepo).save(processedCaptor.capture());
        assertEquals(eventId, processedCaptor.getValue().getEventId());
        assertEquals("thread_created", processedCaptor.getValue().getEventType());
    }

    @Test
    void testHandleThreadCreated_createsNewAggregate() {
        // Given
        when(processedRepo.existsById(anyString())).thenReturn(false);
        when(threadRepo.existsById(123L)).thenReturn(false);

        EventEnvelope event = createEvent("evt-1", "thread_created", Map.of("threadId", 123, "courseId", 42));

        // When
        service.processEvent(event);

        // Then
        ArgumentCaptor<ThreadAggregate> captor = ArgumentCaptor.forClass(ThreadAggregate.class);
        verify(threadRepo).save(captor.capture());
        assertEquals(123L, captor.getValue().getThreadId());
        assertEquals(42L, captor.getValue().getCourseId());
    }

    @Test
    void testHandleCommentAdded_incrementsCommentCount() {
        // Given
        when(processedRepo.existsById(anyString())).thenReturn(false);
        ThreadAggregate existing = new ThreadAggregate(123L, 42L);
        when(threadRepo.findById(123L)).thenReturn(Optional.of(existing));

        EventEnvelope event = createEvent("evt-2", "comment_added", Map.of("threadId", 123, "commentId", 456));

        // When
        service.processEvent(event);

        // Then
        ArgumentCaptor<ThreadAggregate> captor = ArgumentCaptor.forClass(ThreadAggregate.class);
        verify(threadRepo).save(captor.capture());
        assertEquals(1, captor.getValue().getComments());
    }

    @Test
    void testHandleVoteCast_upvoteIncrementsUpvotes() {
        // Given
        when(processedRepo.existsById(anyString())).thenReturn(false);
        ThreadAggregate existing = new ThreadAggregate(123L, 42L);
        when(threadRepo.findById(123L)).thenReturn(Optional.of(existing));

        Map<String, Object> payload = new HashMap<>();
        payload.put("targetType", "THREAD");
        payload.put("targetId", 123);
        payload.put("voteType", "UPVOTE");
        payload.put("userId", 777);

        EventEnvelope event = createEvent("evt-3", "vote_cast", payload);

        // When
        service.processEvent(event);

        // Then
        ArgumentCaptor<ThreadAggregate> captor = ArgumentCaptor.forClass(ThreadAggregate.class);
        verify(threadRepo).save(captor.capture());
        assertEquals(1, captor.getValue().getUpvotes());
        assertEquals(0, captor.getValue().getDownvotes());
    }

    @Test
    void testHandleVoteCast_downvoteIncrementsDownvotes() {
        // Given
        when(processedRepo.existsById(anyString())).thenReturn(false);
        ThreadAggregate existing = new ThreadAggregate(123L, 42L);
        when(threadRepo.findById(123L)).thenReturn(Optional.of(existing));

        Map<String, Object> payload = new HashMap<>();
        payload.put("targetType", "THREAD");
        payload.put("targetId", 123);
        payload.put("voteType", "DOWNVOTE");
        payload.put("userId", 888);

        EventEnvelope event = createEvent("evt-4", "vote_cast", payload);

        // When
        service.processEvent(event);

        // Then
        ArgumentCaptor<ThreadAggregate> captor = ArgumentCaptor.forClass(ThreadAggregate.class);
        verify(threadRepo).save(captor.capture());
        assertEquals(0, captor.getValue().getUpvotes());
        assertEquals(1, captor.getValue().getDownvotes());
    }

    @Test
    void testHandleThreadViewed_incrementsViewCount() {
        // Given
        when(processedRepo.existsById(anyString())).thenReturn(false);
        ThreadAggregate existing = new ThreadAggregate(123L, 42L);
        when(threadRepo.findById(123L)).thenReturn(Optional.of(existing));

        EventEnvelope event = createEvent("evt-5", "thread_viewed", Map.of("threadId", 123, "viewerId", 1010));

        // When
        service.processEvent(event);

        // Then
        ArgumentCaptor<ThreadAggregate> captor = ArgumentCaptor.forClass(ThreadAggregate.class);
        verify(threadRepo).save(captor.capture());
        assertEquals(1, captor.getValue().getViews());
    }

    @Test
    void testProcessEvent_unknownEventTypeIsIgnored() {
        // Given
        when(processedRepo.existsById(anyString())).thenReturn(false);
        EventEnvelope event = createEvent("evt-6", "unknown_event", Map.of());

        // When
        service.processEvent(event);

        // Then: event recorded but no aggregate operation
        verify(threadRepo, never()).save(any());
        verify(processedRepo).save(any(EventProcessed.class));
    }

    private EventEnvelope createEvent(String eventId, String eventType, Map<String, Object> payload) {
        EventEnvelope envelope = new EventEnvelope(
                eventType,
                eventId,
                Instant.now(),
                1,
                "test-service"
        );
        payload.forEach(envelope::addPayload);
        return envelope;
    }
}
