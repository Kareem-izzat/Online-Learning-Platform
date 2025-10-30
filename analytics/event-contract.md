# Analytics Event Contract

This document defines the event envelope, event types, fields, and example payloads for the Online-Learning-Platform Analytics Service. Use these contracts when emitting events from transactional services (Discussion Service, Course Service, Video Service, etc.).

Versioning
- contractVersion: 1
- Each event envelope contains `schemaVersion` to allow evolution.

Event envelope (recommended common fields)

- eventType: string — business event type (e.g., `thread_created`).
- eventId: string (UUID) — unique id for deduplication.
- occurredAt: string (ISO8601 UTC) — when the event happened.
- schemaVersion: integer — event schema version.
- sourceService: string — logical service name (e.g., `discussion-service`).
- traceId: string (optional) — tracing id for debugging.
- payload: object — event-specific data.

Example envelope (shape)

{
  "eventType": "thread_created",
  "eventId": "<uuid>",
  "occurredAt": "2025-10-30T12:34:56Z",
  "schemaVersion": 1,
  "sourceService": "discussion-service",
  "traceId": "<trace-id>",
  "payload": { ... }
}

Guidelines
- Avoid including PII in events; reference users by `userId`.
- Emit events only after the database transaction is committed. Use the outbox pattern or transaction synchronization in Spring.
- Include `eventId` to support idempotent consumers and deduplication.
- Keep events small; store heavy objects in the transactional DB and reference them by id.
- Use consistent naming: `noun_verb` or `verb_noun` — this contract uses `noun_verb` (e.g., `thread_created`).

Common event types (initial set)

1. thread_created
- Purpose: A discussion thread was created.
- payload:
  - threadId: long
  - courseId: long
  - authorId: long
  - title: string
  - category: string (enum: GENERAL, QUESTION, ANNOUNCEMENT, ASSIGNMENT, TECHNICAL)
  - tags: string[]
  - createdAt: ISO8601

2. comment_added
- Purpose: A comment was added to a thread (may be reply).
- payload:
  - commentId: long
  - threadId: long
  - authorId: long
  - parentCommentId: long | null
  - isAnswer: boolean
  - createdAt: ISO8601

3. vote_cast
- Purpose: A user cast a vote on a thread or comment.
- payload:
  - voteId: long
  - userId: long
  - targetType: string (THREAD or COMMENT)
  - targetId: long
  - voteType: string (UPVOTE or DOWNVOTE)
  - createdAt: ISO8601

4. vote_removed
- Purpose: A user removed their vote.
- payload: same as `vote_cast` but with action semantics.

5. thread_viewed
- Purpose: A thread was viewed (important for engagement metrics).
- payload:
  - threadId: long
  - viewerId: long | null (anonymous viewers allowed)
  - sessionId: string (optional)
  - viewedAt: ISO8601

6. thread_updated
- Purpose: Thread metadata changed (title, tags, pinned, locked, status)
- payload:
  - threadId: long
  - updatedFields: map<string, object>
  - updatedAt: ISO8601

7. thread_deleted / comment_deleted
- Purpose: items deleted — include soft-delete flag or event for audit.

Examples

1) thread_created

{
  "eventType": "thread_created",
  "eventId": "a7d9f2d3-...",
  "occurredAt": "2025-10-30T12:34:56Z",
  "schemaVersion": 1,
  "sourceService": "discussion-service",
  "traceId": "trace-123",
  "payload": {
    "threadId": 123,
    "courseId": 42,
    "authorId": 777,
    "title": "How do I fix NullPointer when using XYZ?",
    "category": "QUESTION",
    "tags": ["java","spring"],
    "createdAt": "2025-10-30T12:34:56Z"
  }
}

2) comment_added

{
  "eventType": "comment_added",
  "eventId": "b4c8a1e2-...",
  "occurredAt": "2025-10-30T12:40:10Z",
  "schemaVersion": 1,
  "sourceService": "discussion-service",
  "payload": {
    "commentId": 456,
    "threadId": 123,
    "authorId": 888,
    "parentCommentId": null,
    "isAnswer": false,
    "createdAt": "2025-10-30T12:40:10Z"
  }
}

3) vote_cast

{
  "eventType": "vote_cast",
  "eventId": "c9e3d4f5-...",
  "occurredAt": "2025-10-30T13:01:02Z",
  "schemaVersion": 1,
  "sourceService": "discussion-service",
  "payload": {
    "voteId": 999,
    "userId": 777,
    "targetType": "THREAD",
    "targetId": 123,
    "voteType": "UPVOTE",
    "createdAt": "2025-10-30T13:01:02Z"
  }
}

4) thread_viewed

{
  "eventType": "thread_viewed",
  "eventId": "d2f6e8a0-...",
  "occurredAt": "2025-10-30T13:10:00Z",
  "schemaVersion": 1,
  "sourceService": "discussion-service",
  "payload": {
    "threadId": 123,
    "viewerId": 1010,
    "sessionId": "sess-abc-123",
    "viewedAt": "2025-10-30T13:10:00Z"
  }
}

Publishing guidance
- Prefer asynchronous delivery to a message broker (Kafka, Pulsar). Topics per domain e.g., `discussion.thread`, `discussion.comment`, `discussion.vote`, `discussion.view`.
- For reliability, use an outbox table and a small publisher process to ensure events are published only after DB commit.
- If you cannot run a broker yet, the fastest PoC is an HTTP ingest endpoint; keep requests non-blocking and retryable.

Consumer responsibilities
- Consumers must deduplicate using `eventId` when needed.
- Maintain idempotent updates. For example, when applying `vote_cast`, compute delta (or apply idempotent upsert keyed by `voteId`).
- Store raw events short-term (for replay) and compute/maintain aggregates in an analytical store.

Security
- Use internal authentication to protect ingest endpoints or Kafka ACLs.
- Avoid including user PII; if necessary, encrypt or hash sensitive values.

Change management
- When changing an event schema, increment `schemaVersion` and document the change.
- Prefer additive changes (new optional fields) where possible for backward compatibility.

Outbox pattern snippet (Spring)
- Persist domain entity + an outbox row in same transaction.
- Separate publisher polls outbox and writes messages to Kafka; mark outbox row delivered.

---

This contract is intentionally minimal and pragmatic. If you'd like, I can:
- Generate a JSON Schema (or Avro) and a Postman collection of example publish calls, or
- Scaffold a minimal Analytics module that exposes an HTTP ingest and writes sample aggregates to a local Postgres/Timescale table.

Next suggested step: run a PoC where Discussion Service POSTs `thread_created` and `thread_viewed` to a local Analytics ingest endpoint and verify aggregates update.
