# Progress Service

Student progress tracking microservice for the Online Learning Platform.

## Features

- **Course Progress Tracking**: Overall course completion percentage, certificates
- **Lesson Progress**: Video watch time, lesson completion tracking
- **Quiz Progress**: Quiz attempts, scores, best scores, pass/fail tracking
- **Assignment Progress**: Submission tracking, grading status, scores
- **Student Analytics**: Complete student progress summary across all courses

## API Endpoints

### Course Progress
- `POST /api/progress/courses/initialize` - Initialize course progress for a student
- `GET /api/progress/courses/{studentId}/{courseId}` - Get course progress
- `GET /api/progress/students/{studentId}` - Get all courses progress for a student
- `GET /api/progress/students/{studentId}/summary` - Get complete student summary

### Lesson Progress
- `POST /api/progress/lessons` - Update lesson progress (video time, completion)
- `GET /api/progress/lessons/{studentId}/{courseId}` - Get lesson progress for a course

### Quiz Progress
- `POST /api/progress/quizzes` - Record quiz attempt and score
- `GET /api/progress/quizzes/{studentId}/{courseId}` - Get quiz progress for a course

### Assignment Progress
- `POST /api/progress/assignments` - Record assignment submission/grading
- `GET /api/progress/assignments/{studentId}/{courseId}` - Get assignment progress for a course

## Technologies

- Spring Boot 3.3.4
- Spring Data JPA
- PostgreSQL
- Eureka Client (Service Discovery)
- Lombok

## Database

Create the database:
```sql
CREATE DATABASE progress_service_db;
```

Tables are auto-created by Hibernate:
- `course_progress` - Overall course progress
- `lesson_progress` - Individual lesson tracking
- `quiz_progress` - Quiz attempts and scores
- `assignment_progress` - Assignment submissions and grades

## Running the Service

```bash
cd progress-service
mvn clean install
mvn spring-boot:run
```

Service runs on port **8089**.

## Progress Calculation

**Course Completion Percentage:**
```
completionPercentage = (completedItems / totalItems) * 100

where:
  totalItems = totalLessons + totalQuizzes + totalAssignments
  completedItems = completedLessons + completedQuizzes + completedAssignments
```

**Auto-updates:**
- Course progress recalculates automatically when:
  - A lesson is completed
  - A quiz is submitted
  - An assignment is graded

## Example Workflow

1. **Enrollment**: Call `POST /api/progress/courses/initialize` when student enrolls
2. **Learning**: Update progress as student learns:
   - `POST /api/progress/lessons` - Mark lesson complete
   - `POST /api/progress/quizzes` - Record quiz score
   - `POST /api/progress/assignments` - Record assignment submission
3. **Analytics**: Get summary with `GET /api/progress/students/{studentId}/summary`
