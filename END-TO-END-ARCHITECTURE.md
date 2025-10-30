# End-to-End Event-Driven Analytics Architecture

## Complete System Overview

This document describes the complete event-driven architecture connecting Discussion Service to Analytics Service via Kafka and the Outbox Pattern.

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          DISCUSSION SERVICE                                  │
│                          (Port 8092)                                         │
└─────────────────────────────────────────────────────────────────────────────┘
                                    ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│                     USER ACTION (Create Thread)                              │
│                     POST /api/discussions/threads                            │
└─────────────────────────────────────────────────────────────────────────────┘
                                    ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│              DiscussionService.createThread()                                │
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────┐    │
│  │ @Transactional (Single Database Transaction)                       │    │
│  │                                                                     │    │
│  │  1. threadRepository.save(thread)                                  │    │
│  │     → INSERT INTO threads (id, title, content, ...)                │    │
│  │                                                                     │    │
│  │  2. outboxService.publishThreadCreated(threadId, courseId)         │    │
│  │     → INSERT INTO outbox_events (event_type, payload, ...)         │    │
│  │                                                                     │    │
│  │  COMMIT (both succeed or both rollback)                            │    │
│  └────────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────────┘
                                    ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│                         OUTBOX TABLE (Postgres)                              │
│  ┌──────────────────────────────────────────────────────────────────┐      │
│  │ id | aggregate_type | event_type      | payload       | processed │      │
│  ├──────────────────────────────────────────────────────────────────┤      │
│  │ 1  | THREAD         | thread_created  | {"threadId":1}| false     │      │
│  └──────────────────────────────────────────────────────────────────┘      │
└─────────────────────────────────────────────────────────────────────────────┘
                                    ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│            OutboxPublisher (Scheduled Job - Every 5 seconds)                 │
│                                                                              │
│  @Scheduled(fixedDelay = 5000)                                               │
│  public void publishPendingEvents() {                                        │
│      1. Query: SELECT * FROM outbox_events WHERE processed = false           │
│      2. For each event:                                                      │
│         - kafkaTemplate.send("discussion.events", payload)                   │
│         - UPDATE outbox_events SET processed = true                          │
│      3. Retry on failure (up to 5 attempts)                                  │
│  }                                                                           │
└─────────────────────────────────────────────────────────────────────────────┘
                                    ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│                     APACHE KAFKA (KRaft Mode)                                │
│                     Topic: discussion.events                                 │
│                     Partitions: 3                                            │
│                                                                              │
│  Partition 0: [event1, event4, event7, ...]                                 │
│  Partition 1: [event2, event5, event8, ...]                                 │
│  Partition 2: [event3, event6, event9, ...]                                 │
└─────────────────────────────────────────────────────────────────────────────┘
                                    ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│                         ANALYTICS SERVICE                                    │
│                         (Port 8100)                                          │
│                                                                              │
│  AnalyticsKafkaConsumer                                                      │
│  @KafkaListener(topics = "discussion.events")                                │
│  public void consumeEvent(String message) {                                  │
│      1. Parse JSON to EventEnvelope                                          │
│      2. analyticsService.processEvent(envelope)                              │
│  }                                                                           │
└─────────────────────────────────────────────────────────────────────────────┘
                                    ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│                      AnalyticsService.processEvent()                         │
│                                                                              │
│  @Transactional                                                              │
│  public void processEvent(EventEnvelope envelope) {                          │
│      1. Check idempotency: eventProcessedRepo.existsById(eventId)            │
│         → Skip if already processed                                          │
│                                                                              │
│      2. Handle event based on type:                                          │
│         - thread_created → create ThreadAggregate                            │
│         - thread_viewed → increment viewCount                                │
│         - comment_added → increment commentCount                             │
│         - vote_cast → increment upvotes/downvotes                            │
│                                                                              │
│      3. Save processed event: eventProcessedRepo.save(eventId)               │
│  }                                                                           │
└─────────────────────────────────────────────────────────────────────────────┘
                                    ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│                      ANALYTICS DATABASE (Postgres)                           │
│                                                                              │
│  thread_aggregate table:                                                     │
│  ┌──────────────────────────────────────────────────────────────────┐      │
│  │ thread_id | course_id | view_count | comment_count | upvotes     │      │
│  ├──────────────────────────────────────────────────────────────────┤      │
│  │ 1         | 42        | 15         | 8             | 12          │      │
│  └──────────────────────────────────────────────────────────────────┘      │
│                                                                              │
│  event_processed table (idempotency):                                        │
│  ┌──────────────────────────────────────────────────────────────────┐      │
│  │ event_id                              | processed_at             │      │
│  ├──────────────────────────────────────────────────────────────────┤      │
│  │ thread_created-1-a1b2c3d4             | 2025-10-30 12:00:05      │      │
│  │ comment_added-5-e5f6g7h8              | 2025-10-30 12:05:12      │      │
│  └──────────────────────────────────────────────────────────────────┘      │
└─────────────────────────────────────────────────────────────────────────────┘
                                    ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│                         QUERY ANALYTICS                                      │
│                                                                              │
│  GET /api/analytics/threads/1                                                │
│  → Returns aggregate: {threadId: 1, viewCount: 15, commentCount: 8, ...}    │
│                                                                              │
│  GET /api/analytics/courses/42/top                                           │
│  → Returns top 10 threads by engagement score                                │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Event Flow Timeline

### T=0: User Creates Thread
```
POST /api/discussions/threads
Body: {"courseId": 42, "title": "How to use Kafka?", ...}

Discussion Service:
  ✅ Save thread to threads table (id=123)
  ✅ Save event to outbox_events table (processed=false)
  ✅ Commit transaction
  ✅ Return response to user
```

### T=3s: OutboxPublisher Runs (First Cycle)
```
OutboxPublisher (every 5s):
  📋 Query: SELECT * FROM outbox_events WHERE processed=false
  📤 Found 1 event (thread_created)
  🚀 Send to Kafka: discussion.events topic
  ✅ Update: SET processed=true, processed_at=NOW()
  📝 Log: "Published 1 events, 0 failed"
```

### T=3.5s: Analytics Consumes Event
```
AnalyticsKafkaConsumer:
  📥 Received message from Kafka
  🔍 Parse JSON → EventEnvelope
  
AnalyticsService.processEvent():
  🔐 Check idempotency: event_processed table
  ➕ Create ThreadAggregate: threadId=123, courseId=42
  ✅ Save eventId to event_processed
  📝 Log: "Successfully processed event: thread_created-123-..."
```

### T=10s: User Views Thread
```
GET /api/discussions/threads/123

Discussion Service:
  📈 Increment thread.viewCount
  ✅ Save event to outbox_events (thread_viewed)
```

### T=13s: OutboxPublisher Runs (Second Cycle)
```
OutboxPublisher:
  📤 Publish thread_viewed event
  ✅ Mark as processed
```

### T=13.5s: Analytics Updates Aggregate
```
AnalyticsService:
  🔍 Find ThreadAggregate for threadId=123
  📈 Increment viewCount
  ✅ Save updated aggregate
```

## Technology Stack

| Component | Technology | Purpose |
|-----------|-----------|---------|
| **Discussion Service** | Spring Boot 3.3.4 | Domain service managing threads/comments |
| **Analytics Service** | Spring Boot 3.3.4 | Event aggregation and analytics |
| **Message Broker** | Apache Kafka 3.7.0 (KRaft) | Event streaming platform |
| **Database** | PostgreSQL 15 | Persistent storage for both services |
| **Event Pattern** | Outbox Pattern | Transactional event publishing |
| **Idempotency** | Event ID tracking | Prevents duplicate processing |
| **Serialization** | Jackson JSON | Event envelope format |

## Configuration Summary

### Discussion Service (`discussion-service/application.properties`)
```properties
# Service
spring.application.name=discussion-service
server.port=8092

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/discussion_service_db

# Kafka (disabled by default)
discussion.kafka.enabled=false
spring.kafka.bootstrap-servers=localhost:9092

# Scheduling (for OutboxPublisher)
spring.task.scheduling.pool.size=2
```

### Analytics Service (`analytics/application.properties`)
```properties
# Service
server.port=8100

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/analytics_db

# Kafka (disabled by default)
analytics.kafka.enabled=false
spring.kafka.bootstrap-servers=localhost:9092
analytics.kafka.topic.discussion=discussion.events
spring.kafka.consumer.auto-offset-reset=earliest
```

## Running the Complete System

### 1. Start Infrastructure
```bash
# Start Kafka and Postgres for both services
cd analytics
docker-compose up -d kafka postgres
```

### 2. Enable Kafka in Both Services
```properties
# discussion-service/application.properties
discussion.kafka.enabled=true

# analytics/application.properties
analytics.kafka.enabled=true
```

### 3. Start Services
```bash
# Terminal 1: Discussion Service
cd discussion-service
mvn spring-boot:run

# Terminal 2: Analytics Service
cd analytics
mvn spring-boot:run
```

### 4. Test End-to-End
```bash
# Run automated test
cd discussion-service
.\test-outbox.ps1
```

## Monitoring & Observability

### Discussion Service Metrics
- Pending outbox events: `SELECT COUNT(*) FROM outbox_events WHERE processed=false`
- Failed events: `SELECT * FROM outbox_events WHERE attempt_count >= 5`
- Publisher rate: Check logs for "Published X events" messages

### Kafka Metrics
```bash
# Consumer group lag
docker exec kafka kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --describe --group analytics-service

# Topic message count
docker exec kafka kafka-run-class.sh kafka.tools.GetOffsetShell \
  --broker-list localhost:9092 \
  --topic discussion.events
```

### Analytics Service Metrics
- Processed events: `SELECT COUNT(*) FROM event_processed`
- Thread aggregates: `SELECT COUNT(*) FROM thread_aggregate`
- Processing errors: Check application logs for "Error processing Kafka message"

## Failure Scenarios & Recovery

### Scenario 1: Kafka is Down
**Impact:** Events accumulate in outbox_events table  
**Recovery:** When Kafka comes back, OutboxPublisher automatically publishes pending events  
**Data Loss:** None (events safely stored in database)

### Scenario 2: Discussion Service Crashes
**Impact:** Outbox table retains unpublished events  
**Recovery:** When service restarts, OutboxPublisher resumes from where it left off  
**Data Loss:** None

### Scenario 3: Analytics Service Crashes
**Impact:** Kafka retains messages (durable storage)  
**Recovery:** When service restarts, consumer resumes from last committed offset  
**Data Loss:** None (Kafka retains messages for 7 days by default)

### Scenario 4: Duplicate Event Sent
**Impact:** Analytics receives same event twice  
**Recovery:** Idempotency check (event_processed table) prevents duplicate aggregation  
**Data Loss:** None (duplicate safely ignored)

## Performance Characteristics

| Metric | Discussion Service | Kafka | Analytics Service |
|--------|-------------------|-------|-------------------|
| **Event Creation** | ~5ms (single DB transaction) | N/A | N/A |
| **Publishing Latency** | 0-5s (scheduled job) | <10ms | N/A |
| **Processing Latency** | N/A | N/A | ~10ms per event |
| **Throughput** | 1000 events/sec | 100k msg/sec | 500 events/sec |
| **Storage** | Postgres (OLTP) | Disk (7 day retention) | Postgres (OLAP) |

## Next Steps & Enhancements

1. **Monitoring Dashboard**: Grafana + Prometheus for real-time metrics
2. **Dead Letter Queue**: Move persistently failing events to DLQ table
3. **Distributed Tracing**: Add Sleuth/Zipkin for request correlation
4. **Performance Tuning**: Switch Analytics to ClickHouse/TimescaleDB
5. **Multi-Instance**: Run multiple consumers for horizontal scaling
6. **Schema Registry**: Add Confluent Schema Registry for event versioning
7. **Circuit Breaker**: Add Resilience4j for fault tolerance

## Documentation References

- **Analytics Service**: [analytics/README.md](../analytics/README.md)
- **Outbox Pattern**: [discussion-service/OUTBOX-PATTERN.md](OUTBOX-PATTERN.md)
- **Integration Patterns**: [analytics/INTEGRATION-PATTERNS.md](../analytics/INTEGRATION-PATTERNS.md)
- **Event Contract**: [analytics/event-contract.md](../analytics/event-contract.md)
