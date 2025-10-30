# Discussion Service

Forum and Q&A service for course discussions, enabling students and instructors to create threads, comment, vote, and collaborate.

## Features

- **Discussion Threads**: Create and manage course discussion threads
- **Comments & Replies**: Nested commenting system with up to 3 levels
- **Voting System**: Upvote/downvote threads and comments
- **Best Answer**: Mark comments as best answers for Q&A threads
- **Thread Categories**: GENERAL, QUESTION, ANNOUNCEMENT, ASSIGNMENT, TECHNICAL
- **Search & Filter**: Full-text search and filtering by category, author, course
- **Moderation**: Pin, lock, and manage thread visibility
- **Statistics**: View counts, reply counts, vote counts

## Technology Stack

- Spring Boot 3.3.4
- Spring Data JPA
- PostgreSQL
- Spring Cloud Netflix Eureka Client
- Validation API

## API Endpoints

### Thread Management
- `POST /api/discussions/threads` - Create new thread
- `GET /api/discussions/threads/{id}` - Get thread details
- `GET /api/discussions/threads/course/{courseId}` - List threads by course
- `PUT /api/discussions/threads/{id}` - Update thread
- `DELETE /api/discussions/threads/{id}` - Delete thread
- `PUT /api/discussions/threads/{id}/pin` - Pin/unpin thread
- `PUT /api/discussions/threads/{id}/lock` - Lock/unlock thread

### Comments
- `POST /api/discussions/comments` - Add comment
- `GET /api/discussions/comments/thread/{threadId}` - Get thread comments
- `PUT /api/discussions/comments/{id}` - Update comment
- `DELETE /api/discussions/comments/{id}` - Delete comment
- `PUT /api/discussions/comments/{id}/mark-answer` - Mark as best answer

### Voting
- `POST /api/discussions/votes` - Cast vote (upvote/downvote)
- `DELETE /api/discussions/votes/{id}` - Remove vote
- `GET /api/discussions/votes/user/{userId}/thread/{threadId}` - Get user's vote on thread

### Search & Statistics
- `GET /api/discussions/search` - Search threads by keywords
- `GET /api/discussions/threads/user/{userId}` - Get user's threads
- `GET /api/discussions/threads/{id}/stats` - Get thread statistics

## Configuration

Port: 8092
Database: discussion_service_db

## Running the Service

```bash
mvn spring-boot:run
```
