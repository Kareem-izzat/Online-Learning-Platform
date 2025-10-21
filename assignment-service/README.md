# Assignment Service

The Assignment Service manages homework, quizzes, projects, and student submissions for the Online Learning Platform. It handles assignment creation, submission tracking, grading, and file uploads.

## Features

- **Assignment Management**: Create, update, delete, and publish assignments
- **Multiple Assignment Types**: Multiple choice, essay, file upload, coding challenges
- **Student Submissions**: Submit text responses and file uploads
- **Grading System**: Manual and auto-grading with scoring
- **Late Submissions**: Configurable late submission rules and penalties
- **Attempt Tracking**: Limit submission attempts per assignment
- **Timed Assignments**: Set time limits for quizzes
- **Due Date Management**: Track overdue and upcoming assignments
- **Feedback System**: Provide instructor feedback on submissions
- **Analytics**: Calculate average scores and track completion

## Technology Stack

- **Spring Boot 3.3.4**
- **Spring Data JPA**
- **PostgreSQL**
- **Spring Cloud Netflix Eureka Client**
- **Lombok**
- **Maven**

## Configuration

### Database
- Database Name: `assignment_service_db`
- Port: 5432
- Username: `postgres`
- Password: `123456`

### Service
- Port: 8085
- Service Name: `assignment-service`
- Eureka Server: http://localhost:8761/eureka/

### File Upload
- Max File Size: 50MB
- Upload Directory: `uploads/submissions`

## API Endpoints

### Assignment Endpoints

#### Create Assignment
```
POST /api/assignments
Content-Type: application/json

{
  "courseId": 1,
  "lessonId": 5,
  "title": "Java Basics Quiz",
  "description": "Test your Java knowledge",
  "instructions": "Answer all questions to the best of your ability",
  "type": "MULTIPLE_CHOICE",
  "totalPoints": 100,
  "passingScore": 70,
  "dueDate": "2025-10-30T23:59:59",
  "allowLateSubmission": true,
  "latePenaltyPercent": 10,
  "maxAttempts": 3,
  "timeLimitMinutes": 60,
  "createdBy": 1,
  "published": false
}

Response: 201 Created
```

#### Get All Assignments
```
GET /api/assignments
Response: 200 OK
```

#### Get Assignment by ID
```
GET /api/assignments/{id}
Response: 200 OK
```

#### Get Assignments by Course
```
GET /api/assignments/course/{courseId}
Response: 200 OK
```

#### Get Published Assignments (Students)
```
GET /api/assignments/course/{courseId}/published
Response: 200 OK
```

#### Get Assignments by Lesson
```
GET /api/assignments/lesson/{lessonId}
Response: 200 OK
```

#### Get Assignments by Instructor
```
GET /api/assignments/instructor/{instructorId}
Response: 200 OK
```

#### Get Upcoming Assignments
```
GET /api/assignments/upcoming?daysAhead=7
Response: 200 OK
```

#### Update Assignment
```
PUT /api/assignments/{id}
Content-Type: application/json

{
  "title": "Updated Quiz Title",
  "dueDate": "2025-11-01T23:59:59"
}

Response: 200 OK
```

#### Publish Assignment
```
PUT /api/assignments/{id}/publish
Response: 200 OK
```

#### Unpublish Assignment
```
PUT /api/assignments/{id}/unpublish
Response: 200 OK
```

#### Delete Assignment
```
DELETE /api/assignments/{id}
Response: 204 No Content
```

### Submission Endpoints

#### Submit Assignment
```
POST /api/submissions/submit
Content-Type: multipart/form-data

Parameters:
- assignmentId: 1
- studentId: 5
- submissionText: "My essay text here..." (optional)
- file: [select file] (optional)
- attemptNumber: 1 (optional)
- timeTakenMinutes: 45 (optional)

Response: 201 Created
{
  "id": 1,
  "assignmentId": 1,
  "studentId": 5,
  "status": "SUBMITTED",
  "submissionText": "My essay text here...",
  "fileUrl": "uploads/submissions/uuid-file.pdf",
  "fileName": "essay.pdf",
  "fileSize": 1024000,
  "score": null,
  "feedback": null,
  "attemptNumber": 1,
  "submittedAt": "2025-10-21T14:30:00",
  "isLate": false,
  "timeTakenMinutes": 45
}
```

#### Grade Submission
```
PUT /api/submissions/{id}/grade
Content-Type: application/json

{
  "score": 85,
  "feedback": "Good work! Consider adding more examples.",
  "gradedBy": 1
}

Response: 200 OK
```

#### Get Submissions by Assignment
```
GET /api/submissions/assignment/{assignmentId}
Response: 200 OK
```

#### Get Submissions by Student
```
GET /api/submissions/student/{studentId}
Response: 200 OK
```

#### Get Student's Submission
```
GET /api/submissions/assignment/{assignmentId}/student/{studentId}
Response: 200 OK
```

#### Get Pending Submissions
```
GET /api/submissions/assignment/{assignmentId}/pending
Response: 200 OK
```

#### Get Average Score
```
GET /api/submissions/assignment/{assignmentId}/average-score
Response: 200 OK
Example: 82.5
```

#### Download Submission File
```
GET /api/submissions/{id}/download
Response: 200 OK (binary file)
```

#### Get Submission by ID
```
GET /api/submissions/{id}
Response: 200 OK
```

#### Delete Submission
```
DELETE /api/submissions/{id}
Response: 204 No Content
```

## Assignment Types

- **MULTIPLE_CHOICE**: Quiz with multiple choice questions
- **ESSAY**: Written essay/text submission
- **FILE_UPLOAD**: Submit files (documents, code, etc.)
- **CODING**: Programming assignment
- **MIXED**: Combination of types

## Submission Status Flow

```
NOT_SUBMITTED → SUBMITTED → GRADED
                    ↓
                  LATE (if past due date)
                    ↓
              RESUBMITTED (for multiple attempts)
```

## Database Schema

### assignments table
- `id`: BIGSERIAL PRIMARY KEY
- `course_id`: BIGINT NOT NULL (FK to course-service)
- `lesson_id`: BIGINT (optional FK to lesson)
- `title`: VARCHAR(255) NOT NULL
- `description`: VARCHAR(2000)
- `instructions`: VARCHAR(5000)
- `type`: VARCHAR(50) NOT NULL
- `total_points`: INTEGER NOT NULL
- `passing_score`: INTEGER
- `due_date`: TIMESTAMP
- `allow_late_submission`: BOOLEAN
- `late_penalty_percent`: INTEGER
- `max_attempts`: INTEGER
- `time_limit_minutes`: INTEGER
- `created_by`: BIGINT NOT NULL (FK to user-service)
- `published`: BOOLEAN NOT NULL
- `created_at`: TIMESTAMP NOT NULL
- `updated_at`: TIMESTAMP
- `published_at`: TIMESTAMP

### submissions table
- `id`: BIGSERIAL PRIMARY KEY
- `assignment_id`: BIGINT NOT NULL (FK to assignments)
- `student_id`: BIGINT NOT NULL (FK to user-service)
- `status`: VARCHAR(50) NOT NULL
- `submission_text`: VARCHAR(5000)
- `file_url`: VARCHAR(255)
- `file_name`: VARCHAR(255)
- `file_size`: BIGINT
- `score`: INTEGER
- `feedback`: VARCHAR(2000)
- `attempt_number`: INTEGER
- `submitted_at`: TIMESTAMP
- `graded_at`: TIMESTAMP
- `graded_by`: BIGINT (FK to user-service)
- `is_late`: BOOLEAN
- `time_taken_minutes`: INTEGER
- `created_at`: TIMESTAMP NOT NULL
- `updated_at`: TIMESTAMP

## Building and Running

### Prerequisites
1. PostgreSQL installed and running
2. Create database: `assignment_service_db`
3. Eureka Discovery Service running on port 8761

### Build
```bash
mvn clean install
```

### Run
```bash
mvn spring-boot:run
```

Or run the JAR:
```bash
java -jar target/assignment-service-1.0.0-SNAPSHOT.jar
```

## Testing with Postman

### Create an Assignment
1. POST to `http://localhost:8085/api/assignments`
2. Headers: `Content-Type: application/json`
3. Body: JSON with assignment details
4. Send

### Submit Assignment with File
1. POST to `http://localhost:8085/api/submissions/submit`
2. Body tab → select "form-data"
3. Add keys:
   - `assignmentId`: 1
   - `studentId`: 5
   - `submissionText`: "My answer..."
   - `file` (type: File): Select file
4. Send

### Grade Submission
1. PUT to `http://localhost:8085/api/submissions/1/grade`
2. Headers: `Content-Type: application/json`
3. Body: `{"score": 85, "feedback": "Great work!", "gradedBy": 1}`
4. Send

## Integration with Other Services

- **Course Service**: Assignments linked to courses via `courseId`
- **Enrollment Service**: Only enrolled students can submit assignments
- **User Service**: Tracks instructors (`createdBy`, `gradedBy`) and students (`studentId`)
- **Notification Service**: Can trigger notifications for due dates, grading completion

## Business Logic

### Late Submission Rules
- If `allowLateSubmission = true` and submission is after `dueDate`:
  - Submission marked as `isLate = true`
  - Score reduced by `latePenaltyPercent` when graded
  - Example: Score 90, penalty 10% = Final score 81

### Attempt Limits
- If `maxAttempts = 3`, student can submit up to 3 times
- Each submission tracked with `attemptNumber` (1, 2, 3)
- System tracks highest score across attempts

### Auto-Grading (Future Enhancement)
- Multiple choice questions can be auto-graded
- System compares student answers with correct answers
- Instantly provides score and feedback

## Future Enhancements

- Question bank for multiple choice quizzes
- Plagiarism detection
- Peer review system
- Rubric-based grading
- Auto-grading for coding assignments
- Video submissions
- Group assignments
- Assignment templates
- Export grades to CSV
- LMS integration (Canvas, Moodle)
