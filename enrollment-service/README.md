# Enrollment Service

Microservice for managing student course enrollments and tracking learning progress.

## Features

- Student course enrollment
- Enrollment status management (Active, Completed, Cancelled, Expired)
- Progress tracking (0-100%)
- Automatic completion and certificate issuance
- Query enrollments by user or course
- Active enrollment counting

## Technology Stack

- **Spring Boot 3.3.4** - Framework
- **Spring Data JPA** - Data access
- **PostgreSQL** - Database
- **Spring Cloud Netflix Eureka** - Service discovery
- **Spring Cloud OpenFeign** - Inter-service communication
- **Lombok** - Reduce boilerplate
- **Bean Validation** - Input validation

## API Endpoints

### Enroll Student
```http
POST /api/enrollments
Content-Type: application/json

{
  "userId": 1,
  "courseId": 1
}
```

### Get Enrollment by ID
```http
GET /api/enrollments/{id}
```

### Get Enrollments by User
```http
GET /api/enrollments/user/{userId}
```

### Get Active Enrollments by User
```http
GET /api/enrollments/user/{userId}/active
```

### Get Enrollments by Course
```http
GET /api/enrollments/course/{courseId}
```

### Update Progress
```http
PUT /api/enrollments/{id}/progress?progressPercentage=75
```

### Complete Enrollment
```http
PUT /api/enrollments/{id}/complete
```

### Cancel Enrollment
```http
DELETE /api/enrollments/{id}
```

### Get Active Enrollment Count for Course
```http
GET /api/enrollments/course/{courseId}/count
```

## Database Schema

### enrollments Table
- `id` - Primary key
- `user_id` - Foreign key to User Service
- `course_id` - Foreign key to Course Service
- `status` - ACTIVE, COMPLETED, CANCELLED, EXPIRED
- `enrollment_date` - When student enrolled
- `completion_date` - When course was completed
- `progress_percentage` - 0-100%
- `last_accessed_at` - Last activity timestamp
- `certificate_issued` - Boolean flag
- `created_at` - Record creation timestamp
- `updated_at` - Record update timestamp

## Configuration

### Database Setup
```sql
CREATE DATABASE enrollment_service_db;
```

### Application Properties
- Port: 8083
- Database: enrollment_service_db
- Eureka: http://localhost:8761/eureka/

## Business Logic

### Enrollment Rules
1. User cannot enroll in the same course twice
2. Progress must be between 0-100%
3. Auto-complete when progress reaches 100%
4. Certificate issued upon completion

### Auto-Completion
When progress reaches 100%:
- Status changes to COMPLETED
- Completion date is set
- Certificate is issued

## Running the Service

```bash
# Build
mvn clean install

# Run
mvn spring-boot:run
```

## Service Discovery
Registers with Eureka as: `ENROLLMENT-SERVICE`

## Inter-Service Communication
- Communicates with **User Service** (via Feign) to verify users
- Communicates with **Course Service** (via Feign) to verify courses

## Testing with Postman

1. **Enroll a student**: POST with userId and courseId
2. **Check enrollment**: GET by enrollment ID
3. **Update progress**: PUT to /progress endpoint
4. **View user enrollments**: GET by userId
5. **Complete course**: PUT to /complete endpoint

## Future Enhancements
- Add Feign clients for User/Course verification
- Email notifications on enrollment
- Certificate generation
- Progress analytics
- Enrollment expiration logic
