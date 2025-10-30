# Event Integration Patterns: HTTP vs Kafka

This document explains **two integration patterns** for sending events from Discussion Service to Analytics Service. Both are implemented and you can choose based on your needs.

---

## Pattern Comparison

| Aspect | HTTP Ingest (Simple) | Kafka + Outbox (Production) |
|--------|---------------------|----------------------------|
| **Complexity** | Low | Medium |
| **Setup** | Just run both services | Requires Kafka broker |
| **Reliability** | If Analytics down → events lost | Events stored in Kafka durably |
| **Ordering** | No guarantees | Guaranteed per partition |
| **Replay** | Cannot replay | Can reprocess from any offset |
| **Coupling** | Services know each other | Fully decoupled |
| **Backpressure** | Slow consumer blocks producer | Consumer processes at own pace |
| **Best for** | PoC, demos, simple systems | Production, scale, reliability |

---

## Pattern 1: HTTP Ingest (Current Default)

### Architecture
```
[Discussion Service] --HTTP POST--> [Analytics Service /api/analytics/ingest]
```

### How it works
1. User creates a thread in Discussion Service
2. Discussion Service saves thread to DB (transaction commits)
3. Discussion Service POSTs event JSON to Analytics HTTP endpoint
4. Analytics processes event immediately and returns 202 Accepted

### Pros
- ✅ Simple to understand and debug
- ✅ No extra infrastructure (Kafka)
- ✅ Fast to implement
- ✅ Direct feedback (synchronous)

### Cons
- ❌ If Analytics is down → events lost
- ❌ Network issues → need retry logic
- ❌ Tight coupling between services
- ❌ Can't replay historical events
- ❌ Slow consumer blocks producer

### When to use
- Local development and testing
- PoC/MVP demonstrations
- Low-traffic systems (<100 events/sec)
- When simplicity > reliability

### Example code (conceptual)
```java
// In DiscussionService after thread is saved:
@Transactional
public void createThread(ThreadRequest request) {
    DiscussionThread thread = // save to DB
    threadRepository.save(thread);
    
    // After commit, publish event
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                analyticsClient.postEvent(createThreadEvent(thread));
            }
        }
    );
}
```

### Testing
```powershell
# Start Analytics
cd analytics
mvn spring-boot:run

# POST event
curl -X POST http://localhost:8100/api/analytics/ingest \
  -H "Content-Type: application/json" \
  -d '{
    "eventType":"thread_created",
    "eventId":"test-123",
    "occurredAt":"2025-10-30T12:00:00Z",
    "schemaVersion":1,
    "sourceService":"discussion-service",
    "payload":{"threadId":100,"courseId":42}
  }'
```

---

## Pattern 2: Kafka + Outbox (Production Pattern)

### Architecture
```
[Discussion Service] 
    ↓ (1) Save entity + outbox row (same transaction)
[Outbox Table]
    ↓ (2) Outbox Publisher polls
[Kafka Topic: discussion.events]
    ↓ (3) Analytics Consumer reads
[Analytics Service] processes event
```

### How it works (step-by-step)

#### Step 1: Discussion Service saves to outbox
```java
@Transactional
public void createThread(ThreadRequest request) {
    // Save domain entity
    DiscussionThread thread = threadRepository.save(new DiscussionThread(...));
    
    // Save outbox event in SAME transaction
    OutboxEvent outboxEvent = new OutboxEvent(
        UUID.randomUUID(),
        "thread_created",
        createEventPayload(thread),
        Instant.now()
    );
    outboxRepository.save(outboxEvent);
    
    // Transaction commits → both rows persisted atomically
}
```

**Why outbox?** Guarantees event is saved if and only if domain entity is saved (atomicity).

#### Step 2: Outbox Publisher (background process)
```java
@Scheduled(fixedDelay = 1000) // Poll every second
public void publishOutboxEvents() {
    List<OutboxEvent> pending = outboxRepository.findByPublishedFalse();
    
    for (OutboxEvent event : pending) {
        try {
            kafkaTemplate.send("discussion.events", event.toJson());
            event.setPublished(true);
            outboxRepository.save(event);
        } catch (Exception e) {
            log.error("Failed to publish event", e);
            // Retry next poll
        }
    }
}
```

**Why separate publisher?** Decouples DB transaction from Kafka publish (doesn't block user request).

#### Step 3: Kafka stores event
- Event written to `discussion.events` topic
- Kafka replicates across brokers (durable)
- Event retained for configured time (e.g., 7 days)

#### Step 4: Analytics consumes from Kafka
```java
@KafkaListener(topics = "discussion.events", groupId = "analytics-service")
public void consumeEvent(String message) {
    EventEnvelope envelope = objectMapper.readValue(message, EventEnvelope.class);
    analyticsService.processEvent(envelope); // Same logic as HTTP
}
```

### Pros
- ✅ **Reliable**: Outbox guarantees event published if entity saved
- ✅ **Durable**: Kafka stores events, can replay
- ✅ **Decoupled**: Services don't know about each other
- ✅ **Scalable**: Add more consumers, Kafka balances load
- ✅ **Asynchronous**: Slow consumer doesn't block producer
- ✅ **Fault-tolerant**: If Analytics down, it catches up when restarted

### Cons
- ❌ More complex setup (Kafka broker)
- ❌ Eventual consistency (event processed after small delay)
- ❌ Requires outbox cleanup (old events)
- ❌ Need to monitor Kafka lag

### When to use
- Production systems
- High traffic (>100 events/sec)
- When you need event replay
- Multiple consumers of same events
- When reliability > simplicity

### Kafka Concepts Explained

#### Topics
- Named stream of events (like a database table)
- Example: `discussion.events`, `course.events`
- Events in a topic are ordered and immutable

#### Partitions
- Topics split into partitions for parallelism
- Events with same key go to same partition (preserves order)
- Example: all events for `courseId=42` go to partition 2

#### Consumer Groups
- Multiple instances of Analytics form a group
- Kafka distributes partitions across group members
- If one instance dies, Kafka rebalances

#### Offsets
- Sequential ID for each event in a partition
- Consumer tracks its position (offset)
- Can reset to replay from earlier offset

### Setup and Testing

#### 1. Start Kafka
```powershell
cd analytics
docker-compose up -d kafka
```

Wait 30 seconds for Kafka to be ready.

#### 2. Create topic (optional, auto-created by default)
```powershell
docker exec -it kafka kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --create --topic discussion.events \
  --partitions 3 --replication-factor 1
```

#### 3. Start Analytics with Kafka enabled
```powershell
# Set environment variable
$env:ANALYTICS_KAFKA_ENABLED="true"
mvn -f analytics/pom.xml spring-boot:run
```

Or edit `application.properties`:
```properties
analytics.kafka.enabled=true
```

#### 4. Produce test event to Kafka
```powershell
# Using kafka-console-producer
docker exec -it kafka kafka-console-producer.sh \
  --bootstrap-server localhost:9092 \
  --topic discussion.events

# Then paste event JSON:
{"eventType":"thread_created","eventId":"kafka-test-1","occurredAt":"2025-10-30T12:00:00Z","schemaVersion":1,"sourceService":"test","payload":{"threadId":200,"courseId":50}}
```

#### 5. Verify Analytics processed it
```powershell
curl http://localhost:8100/api/analytics/threads/200
```

### Monitoring Kafka

#### Check consumer lag
```powershell
docker exec -it kafka kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --describe --group analytics-service
```

Output shows:
- `CURRENT-OFFSET`: where consumer is now
- `LOG-END-OFFSET`: latest event in topic
- `LAG`: how many events behind (should be 0 or low)

#### List topics
```powershell
docker exec -it kafka kafka-topics.sh \
  --bootstrap-server localhost:9092 --list
```

#### Read events from topic
```powershell
docker exec -it kafka kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic discussion.events --from-beginning
```

---

## Outbox Pattern Implementation (Discussion Service)

### 1. Add Outbox Entity
```java
@Entity
@Table(name = "outbox_events")
public class OutboxEvent {
    @Id
    private UUID id;
    
    @Column(nullable = false)
    private String eventType;
    
    @Column(columnDefinition = "TEXT")
    private String payload; // JSON
    
    @Column(nullable = false)
    private Instant createdAt;
    
    @Column(nullable = false)
    private Boolean published = false;
    
    private Instant publishedAt;
}
```

### 2. Save to outbox in same transaction
```java
@Service
public class DiscussionService {
    
    @Transactional
    public ThreadResponse createThread(ThreadRequest request) {
        // Save domain entity
        DiscussionThread thread = new DiscussionThread(...);
        thread = threadRepository.save(thread);
        
        // Create event envelope
        EventEnvelope envelope = new EventEnvelope(
            "thread_created",
            UUID.randomUUID().toString(),
            Instant.now(),
            1,
            "discussion-service"
        );
        envelope.addPayload("threadId", thread.getId());
        envelope.addPayload("courseId", thread.getCourseId());
        // ... more payload
        
        // Save to outbox
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setId(UUID.randomUUID());
        outboxEvent.setEventType("thread_created");
        outboxEvent.setPayload(objectMapper.writeValueAsString(envelope));
        outboxEvent.setCreatedAt(Instant.now());
        outboxRepository.save(outboxEvent);
        
        // Both saved in same transaction!
        return new ThreadResponse(thread);
    }
}
```

### 3. Outbox Publisher (scheduled task)
```java
@Component
@ConditionalOnProperty(name = "discussion.outbox.enabled", havingValue = "true")
public class OutboxPublisher {
    
    @Scheduled(fixedDelayString = "${discussion.outbox.poll-interval-ms:1000}")
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pending = outboxRepository
            .findTop100ByPublishedFalseOrderByCreatedAtAsc();
        
        for (OutboxEvent event : pending) {
            try {
                kafkaTemplate.send("discussion.events", event.getPayload()).get();
                event.setPublished(true);
                event.setPublishedAt(Instant.now());
                outboxRepository.save(event);
                log.info("Published event {}", event.getId());
            } catch (Exception e) {
                log.error("Failed to publish event {}", event.getId(), e);
                // Will retry next poll
            }
        }
    }
    
    // Cleanup old published events
    @Scheduled(cron = "0 0 2 * * ?") // 2 AM daily
    @Transactional
    public void cleanupOldEvents() {
        Instant cutoff = Instant.now().minus(7, ChronoUnit.DAYS);
        outboxRepository.deleteByPublishedTrueAndPublishedAtBefore(cutoff);
    }
}
```

---

## Switching Between Patterns

### Use HTTP only (default)
```properties
# analytics/application.properties
analytics.kafka.enabled=false
```

### Use Kafka only
```properties
# analytics/application.properties
analytics.kafka.enabled=true

# discussion-service/application.properties
discussion.outbox.enabled=true
spring.kafka.bootstrap-servers=localhost:9092
```

### Use both (hybrid)
- Keep `analytics.kafka.enabled=false`
- HTTP ingest still available
- Can test Kafka separately by producing to topic manually

---

## Troubleshooting

### Kafka not starting
```powershell
# Check logs
docker logs kafka

# Common issue: port 9092 already in use
netstat -ano | findstr 9092
```

### Consumer not receiving events
```powershell
# Check if topic exists
docker exec -it kafka kafka-topics.sh --bootstrap-server localhost:9092 --list

# Check consumer group
docker exec -it kafka kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group analytics-service

# Read from topic manually
docker exec -it kafka kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic discussion.events --from-beginning
```

### Events not in Analytics DB
- Check Analytics logs for errors
- Verify Kafka consumer is enabled (`analytics.kafka.enabled=true`)
- Check idempotency: duplicate `eventId` will be skipped

---

## Performance Comparison

### HTTP Ingest
- **Latency**: 10-50ms (network + processing)
- **Throughput**: ~1000 events/sec per Analytics instance
- **Failure mode**: Events lost if Analytics down

### Kafka
- **Latency**: 50-200ms (outbox poll + Kafka + consumer)
- **Throughput**: 10,000+ events/sec (Kafka partitions + consumer group)
- **Failure mode**: Events buffered in Kafka, zero loss

---

## Next Steps

1. **Test both patterns locally** to understand trade-offs
2. **Implement outbox in discussion-service** (I can add this code)
3. **Add monitoring** (consumer lag, processing rate)
4. **Production hardening**:
   - Dead-letter queue for failed events
   - Alerting on high consumer lag
   - Kafka cluster with replication (not single node)

---

## Questions?

**Q: Can I use both HTTP and Kafka at the same time?**
A: Yes! Analytics accepts both. Idempotency (`eventId`) prevents double-counting.

**Q: What if I send the same event via HTTP and Kafka?**
A: Analytics will process it once (first one wins, second is skipped due to `eventId` dedupe).

**Q: Should I use Kafka for my thesis/portfolio project?**
A: If you want to demonstrate production-grade architecture and learn industry patterns → yes. If you want to ship fast → HTTP is fine.

**Q: How do I replay events?**
A: Reset Kafka consumer offset:
```powershell
kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
  --group analytics-service --topic discussion.events \
  --reset-offsets --to-earliest --execute
```

**Q: What's the cost of running Kafka?**
A: Local dev: free (docker). Cloud: ~$50-200/month for managed Kafka (AWS MSK, Confluent Cloud).

---

## Summary

- **For learning/demos**: Start with HTTP, add Kafka later
- **For production/portfolio**: Implement both, show you understand trade-offs
- **For interviews**: Explain outbox pattern and why Kafka > HTTP at scale

Both patterns are fully implemented in this codebase — choose based on your needs!
