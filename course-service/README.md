# Course Service

Course creation and management service for the Online Learning Platform.

## Overview
This service handles all course-related operations including course creation, updates, publishing, and curriculum management with modules and lessons.

## Configuration
- **Port:** 8082
- **Database:** PostgreSQL (course_service_db)
- **Registered with:** Eureka Discovery Service

## Features
- ✅ Create, update, delete courses
- ✅ Course publishing workflow (DRAFT → PUBLISHED)
- ✅ Multi-level curriculum (Course → Modules → Lessons)
- ✅ Course filtering by instructor, status
- ✅ Full course metadata (price, level, duration, language)
- ✅ Instructor-specific course management

## Entities

### Course
- Title, Description, Thumbnail
- Instructor ID (references User Service)
- Price (BigDecimal)
- Level (BEGINNER, INTERMEDIATE, ADVANCED, ALL_LEVELS)
- Status (DRAFT, PUBLISHED, ARCHIVED)
- Duration, Language
- Timestamps (created, updated, published)

### Module
- Course sections/chapters
- Title, Description
- Order index for sequencing
- Contains multiple lessons

### Lesson
- Individual course content
- Title, Content, Video URL
- Duration in minutes
- Free/Premium flag
- Order index within module

## API Endpoints

### Course Management
```
POST   /api/courses                    - Create new course
GET    /api/courses                    - Get all courses
GET    /api/courses/{id}               - Get course by ID
GET    /api/courses/instructor/{id}    - Get courses by instructor
GET    /api/courses/published          - Get all published courses
PUT    /api/courses/{id}               - Update course
PUT    /api/courses/{id}/publish       - Publish course
DELETE /api/courses/{id}               - Delete course
```

## Database Setup

### Option 1: Using pgAdmin
1. Open pgAdmin
2. Right-click on "Databases" → Create → Database
3. Name: `course_service_db`
4. Owner: `postgres`
5. Click Save

### Option 2: Using psql
```bash
psql -U postgres
CREATE DATABASE course_service_db;
\q
```

## Running the Service

### Using Maven
```bash
cd course-service
mvn spring-boot:run
```

### Using Spring Boot Dashboard
1. Open VS Code Spring Boot Dashboard
2. Click play button next to `course-service`

## Testing Examples

### Create a Course
```powershell
Invoke-RestMethod -Uri 'http://localhost:8082/api/courses' -Method Post `
  -ContentType 'application/json' `
  -Body (@{
    title = "Complete Java Programming"
    description = "Master Java from basics to advanced"
    instructorId = 2
    price = 49.99
    level = "BEGINNER"
    durationHours = 40
    language = "English"
  } | ConvertTo-Json)
```

### Get All Courses
```powershell
Invoke-RestMethod -Uri 'http://localhost:8082/api/courses' -Method Get
```

### Publish a Course
```powershell
Invoke-RestMethod -Uri 'http://localhost:8082/api/courses/1/publish' -Method Put
```

## Dependencies
- Spring Boot Web
- Spring Data JPA
- PostgreSQL Driver
- Spring Cloud Eureka Client
- Lombok
- Jakarta Validation

## Next Features
- Module and Lesson CRUD endpoints
- Course enrollment tracking
- Course ratings and reviews
- Course search and filtering
- Course categories/tags
