# Analytics Service (PoC)

This is a minimal Analytics Service proof-of-concept for the Online-Learning-Platform. It supports **two integration patterns**: simple HTTP ingest and production-grade Kafka with outbox pattern.

## Integration Patterns

📖 **See [INTEGRATION-PATTERNS.md](INTEGRATION-PATTERNS.md) for detailed comparison of HTTP vs Kafka patterns**

### Quick Overview
- **HTTP Ingest** (default): Simple POST endpoint, good for PoC/demos
- **Kafka Consumer** (optional): Production-grade event streaming with outbox pattern

## What it does
- Accepts events via HTTP POST or Kafka consumer
- Processes events: `thread_created`, `comment_added`, `vote_cast` (for THREAD), and `thread_viewed`
- Updates `thread_aggregate` table with counters (views, comments, upvotes, downvotes)
- Provides idempotency (duplicate `eventId` prevented)
- Batch ingest support

## Quickstart (HTTP mode - default)

1. Start Postgres using Docker Compose:
   ```bash
   cd analytics
   docker-compose up -d postgres
   ```

2. Build and run the service (from repository root):
   ```bash
   # from repo root
   mvn -f analytics/pom.xml spring-boot:run
   ```

3. Send events using `analytics/sample-events.json` (example using curl):
   ```bash
   # POST single event
   curl -X POST http://localhost:8100/api/analytics/ingest \
     -H "Content-Type: application/json" \
     -d '{"eventType":"thread_created","eventId":"t-001","occurredAt":"2025-01-30T12:00:00Z","schemaVersion":1,"sourceService":"discussion-service","payload":{"threadId":123,"courseId":42}}'
   
   # POST batch of events
   curl -X POST http://localhost:8100/api/analytics/ingest/batch \
     -H "Content-Type: application/json" \
     -d @analytics/sample-events.json
   ```

4. Query aggregates:
   ```bash
   # Get a thread aggregate
   curl http://localhost:8100/api/analytics/threads/123
   
   # Get top threads for a course (default limit 10)
   curl http://localhost:8100/api/analytics/courses/42/top
   ```

## Quickstart (Kafka mode - production)

1. Start infrastructure:
   ```powershell
   cd analytics
   docker-compose up -d postgres kafka
   ```
   Wait ~30 seconds for Kafka to be ready.

2. Enable Kafka in `application.properties`:
   ```properties
   analytics.kafka.enabled=true
   ```

3. Run the service:
   ```powershell
   mvn -f analytics/pom.xml spring-boot:run
   ```

4. Test Kafka integration:
   ```powershell
   .\test-kafka.ps1
   ```
   
   Or manually produce event:
   ```powershell
   docker exec -it kafka kafka-console-producer.sh \
     --bootstrap-server localhost:9092 \
     --topic discussion.events
   
   # Paste event JSON:
   {"eventType":"thread_created","eventId":"k-001","occurredAt":"2025-10-30T12:00:00Z","schemaVersion":1,"sourceService":"test","payload":{"threadId":200,"courseId":50}}
   ```

## Monitoring

### Check Kafka Consumer Status
```bash
# Check consumer group lag
docker exec -it kafka kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --describe --group analytics-service

# View events in topic
docker exec -it kafka kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic discussion.events --from-beginning
```

### Check Database
```bash
# Connect to Postgres
docker exec -it analytics-postgres psql -U analytics -d analytics_db

# Query aggregates
SELECT * FROM thread_aggregate ORDER BY last_updated DESC LIMIT 10;

# Check processed events (idempotency)
SELECT COUNT(*) FROM event_processed;
```

### Application Health
```bash
# Spring Boot Actuator endpoints (if enabled)
curl http://localhost:8100/actuator/health
curl http://localhost:8100/actuator/metrics
```

## Architecture Notes

### Event Flow (HTTP Pattern)
```
Discussion Service → HTTP POST → Analytics Service → PostgreSQL
                                       ↓
                              [EventProcessed table]
                              (prevents duplicates)
```

### Event Flow (Kafka Pattern)
```
Discussion Service → [Outbox table] → OutboxPublisher → Kafka Topic
                                                            ↓
                                            AnalyticsKafkaConsumer → PostgreSQL
                                                            ↓
                                                  [EventProcessed table]
                                                  (prevents duplicates)
```

### Key Components
- **AnalyticsController**: HTTP endpoints for ingest and query
- **AnalyticsService**: Core business logic with idempotency
- **AnalyticsKafkaConsumer**: Kafka message listener (conditional)
- **ThreadAggregate**: JPA entity for aggregated metrics
- **EventProcessed**: Idempotency tracking (stores processed eventIds)

## Testing

Run unit and integration tests:
```bash
mvn -f analytics/pom.xml test
```

Test coverage includes:
- Idempotency enforcement
- Event handler logic (thread_created, comment_added, vote_cast, thread_viewed)
- Batch processing
- Unknown event handling
- End-to-end HTTP ingest with H2

## Notes

- This PoC uses synchronous processing. For production, Kafka with outbox pattern provides better reliability.
- Consider migrating to OLAP store (ClickHouse/TimescaleDB) for high-volume time-series analytics.
- Add monitoring: Prometheus metrics, distributed tracing (Sleuth/Zipkin), consumer lag alerts.

## Next Steps

✅ **Completed:**
- HTTP ingest with idempotency
- Kafka consumer with conditional activation
- Batch ingest support
- Query endpoints
- Comprehensive documentation (INTEGRATION-PATTERNS.md)
- Test automation (test-kafka.ps1)

🔜 **Recommended:**
- Implement outbox pattern in `discussion-service`
- Add Prometheus metrics and Grafana dashboards
- Implement dead-letter queue for failed Kafka events
- Add pagination to query endpoints
- Create time-series tables for trend analysis
