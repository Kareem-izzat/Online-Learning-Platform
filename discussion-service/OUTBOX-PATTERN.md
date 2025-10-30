# Discussion Service - Outbox Pattern Integration

## Overview

Discussion Service now implements the **Outbox Pattern** for reliable event publishing to Kafka. This ensures that events are never lost, even if Kafka is temporarily unavailable.

## How It Works

### 1. Transactional Event Creation
When a domain event occurs (thread created, comment added, vote cast), the service:
1. Saves the domain entity to the database (e.g., `threads` table)
2. Saves an event record to `outbox_events` table **in the same transaction**
3. Both succeed or both rollback (atomicity guaranteed)

```java
@Transactional
public ThreadResponse createThread(ThreadRequest request) {
    // Save thread to database
    DiscussionThread savedThread = threadRepository.save(thread);
    
    // Save event to outbox in SAME transaction
    outboxService.publishThreadCreated(savedThread.getId(), savedThread.getCourseId());
    
    return new ThreadResponse(savedThread);
}
```

### 2. Background Publishing
`OutboxPublisher` runs every 5 seconds and:
1. Queries `outbox_events` where `processed = false`
2. Publishes each event to Kafka topic `discussion.events`
3. Marks event as `processed = true` with timestamp
4. Retries failed events (up to 5 attempts)

### 3. Event Flow Diagram

```
User Action (Create Thread)
    ↓
┌──────────────────────────────────────────────┐
│ Database Transaction                         │
│                                              │
│  INSERT INTO threads (...)                   │ ✅
│  INSERT INTO outbox_events (...)             │ ✅
│                                              │
│  COMMIT (both or neither)                    │
└──────────────────────────────────────────────┘
    ↓
[Event waits in outbox_events table]
    ↓
┌──────────────────────────────────────────────┐
│ OutboxPublisher (every 5 seconds)            │
│                                              │
│  SELECT * FROM outbox_events                 │
│  WHERE processed = false                     │
│      ↓                                       │
│  kafka.send("discussion.events", payload)    │
│      ↓                                       │
│  UPDATE outbox_events SET processed = true   │
└──────────────────────────────────────────────┘
    ↓
Kafka Topic: discussion.events
    ↓
Analytics Service Consumes Event
```

## Components

### OutboxEvent Entity
Stores events before Kafka publishing:
- `aggregateType`: THREAD, COMMENT, VOTE
- `aggregateId`: Entity ID (threadId, commentId, voteId)
- `eventType`: thread_created, comment_added, vote_cast, thread_viewed
- `payload`: JSON EventEnvelope matching analytics contract
- `processed`: Boolean flag (false = pending, true = published)
- `attemptCount`: Retry counter
- `lastError`: Error message if failed

### OutboxService
Creates outbox events within transactions:
- `publishThreadCreated(threadId, courseId)`
- `publishThreadViewed(threadId, courseId)`
- `publishCommentAdded(commentId, threadId, courseId)`
- `publishVoteCast(voteId, targetType, targetId, voteType, threadId, courseId)`

### OutboxPublisher
Background job that publishes to Kafka:
- Runs every 5 seconds (`@Scheduled(fixedDelay = 5000)`)
- Batch processes up to 100 events
- Retries failed events (max 5 attempts)
- Cleans up old processed events (keeps 7 days)

## Configuration

### Enable/Disable Kafka Publishing

**HTTP Mode (default):** Events saved to outbox but not published
```properties
discussion.kafka.enabled=false
```

**Kafka Mode:** Events published to Kafka
```properties
discussion.kafka.enabled=true
spring.kafka.bootstrap-servers=localhost:9092
```

### Kafka Producer Settings
```properties
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.acks=all
spring.kafka.producer.retries=3
```

### Scheduling Configuration
```properties
spring.task.scheduling.pool.size=2
```

## Running with Kafka

### 1. Start Infrastructure
```bash
# Start Postgres for discussion service
docker run -d --name discussion-postgres \
  -e POSTGRES_DB=discussion_service_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=123456 \
  -p 5432:5432 postgres:15

# Start Kafka (from analytics directory)
cd ../analytics
docker-compose up -d kafka
```

### 2. Enable Kafka in application.properties
```properties
discussion.kafka.enabled=true
```

### 3. Run Discussion Service
```bash
mvn spring-boot:run
```

### 4. Run Analytics Service (to consume events)
```bash
cd ../analytics
# Enable Kafka consumer
# In analytics/src/main/resources/application.properties:
# analytics.kafka.enabled=true

mvn spring-boot:run
```

## Testing the Integration

### 1. Create a Thread
```bash
curl -X POST http://localhost:8092/api/discussions/threads \
  -H "Content-Type: application/json" \
  -d '{
    "courseId": 42,
    "authorId": 100,
    "title": "How to use Spring Boot?",
    "content": "I need help with Spring Boot configuration",
    "category": "QUESTION",
    "tags": ["spring-boot", "java"]
  }'
```

### 2. Check Outbox Table
```sql
-- Connect to discussion database
SELECT * FROM outbox_events WHERE processed = false;

-- Should see event with eventType = 'thread_created'
-- Wait 5-10 seconds for OutboxPublisher to run
-- Then:
SELECT * FROM outbox_events WHERE processed = true;
```

### 3. Verify in Kafka
```bash
# View events in Kafka topic
docker exec -it kafka kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic discussion.events \
  --from-beginning
```

### 4. Check Analytics Service
```bash
# Get thread aggregate (should have viewCount = 0, initially)
curl http://localhost:8100/api/analytics/threads/1

# Get thread (triggers thread_viewed event)
curl http://localhost:8092/api/discussions/threads/1

# Wait 5 seconds, check analytics again (viewCount should increment)
curl http://localhost:8100/api/analytics/threads/1
```

## Monitoring

### Check Pending Events
```sql
SELECT COUNT(*) FROM outbox_events WHERE processed = false;
```

### Check Failed Events
```sql
SELECT * FROM outbox_events 
WHERE processed = false AND attempt_count >= 5
ORDER BY created_at DESC;
```

### Check Publisher Logs
```bash
# In discussion service logs, look for:
[OutboxPublisher] Found 3 pending outbox events, publishing to Kafka...
[OutboxPublisher] Published 3 events, 0 failed
```

## Event Types Published

### thread_created
Published when: New thread is created
```json
{
  "eventType": "thread_created",
  "eventId": "thread_created-123-a1b2c3d4",
  "occurredAt": "2025-10-30T12:00:00",
  "schemaVersion": 1,
  "sourceService": "discussion-service",
  "payload": {
    "threadId": 123,
    "courseId": 42
  }
}
```

### thread_viewed
Published when: Thread is retrieved via GET /threads/{id}
```json
{
  "eventType": "thread_viewed",
  "eventId": "thread_viewed-123-e5f6g7h8",
  "occurredAt": "2025-10-30T12:05:00",
  "schemaVersion": 1,
  "sourceService": "discussion-service",
  "payload": {
    "threadId": 123,
    "courseId": 42
  }
}
```

### comment_added
Published when: Comment is added to a thread
```json
{
  "eventType": "comment_added",
  "eventId": "comment_added-456-i9j0k1l2",
  "occurredAt": "2025-10-30T12:10:00",
  "schemaVersion": 1,
  "sourceService": "discussion-service",
  "payload": {
    "commentId": 456,
    "threadId": 123,
    "courseId": 42
  }
}
```

### vote_cast
Published when: Vote (upvote/downvote) is cast on thread or comment
```json
{
  "eventType": "vote_cast",
  "eventId": "vote_cast-789-m3n4o5p6",
  "occurredAt": "2025-10-30T12:15:00",
  "schemaVersion": 1,
  "sourceService": "discussion-service",
  "payload": {
    "voteId": 789,
    "targetType": "THREAD",
    "targetId": 123,
    "voteType": "UPVOTE",
    "threadId": 123,
    "courseId": 42
  }
}
```

## Benefits of Outbox Pattern

✅ **Guaranteed Delivery**: Events never lost, even if Kafka is down  
✅ **Transactional Consistency**: Domain entity and event saved atomically  
✅ **Automatic Retries**: Failed publishes retry every 5 seconds  
✅ **Audit Trail**: All events stored in database  
✅ **Decoupling**: Service doesn't wait for Kafka response  
✅ **Idempotency**: EventIds prevent duplicate processing in analytics  

## Troubleshooting

### Events Not Publishing
1. Check Kafka is running: `docker ps | grep kafka`
2. Check Kafka enabled: `discussion.kafka.enabled=true` in properties
3. Check OutboxPublisher logs for errors
4. Verify outbox table has pending events: `SELECT * FROM outbox_events WHERE processed = false`

### High Pending Event Count
1. Check Kafka connectivity: `spring.kafka.bootstrap-servers=localhost:9092`
2. Check for errors in OutboxPublisher logs
3. Verify Kafka topic exists: `docker exec -it kafka kafka-topics.sh --list --bootstrap-server localhost:9092`

### Events Stuck After Max Retries
1. Query failed events: `SELECT * FROM outbox_events WHERE attempt_count >= 5`
2. Check `last_error` column for failure reason
3. Fix issue (e.g., Kafka connectivity)
4. Reset `processed = false, attempt_count = 0` to retry

## Production Considerations

1. **Dead Letter Queue**: Move events after max retries to DLQ table
2. **Monitoring**: Add Prometheus metrics for pending count, failure rate
3. **Alerting**: Alert when pending count > threshold or failures occur
4. **Partitioning**: Partition outbox table by created_at for performance
5. **Async Publishing**: Use KafkaTemplate async methods for throughput
6. **Multiple Publishers**: Run multiple instances for high load
7. **Cleanup Job**: Tune retention period based on debugging needs
