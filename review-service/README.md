# Review Service

Course review and rating microservice for the Online Learning Platform.

## Features

- **Star Ratings**: 1-5 star ratings for courses
- **Written Reviews**: Detailed text reviews (up to 2000 characters)
- **Verified Reviews**: Mark reviews from verified enrollments
- **Instructor Responses**: Instructors can respond to reviews
- **Review Helpfulness**: Users can vote reviews as helpful/not helpful
- **Rating Statistics**: Average ratings, star distribution, total reviews
- **Review Moderation**: Approve/reject/flag reviews (for admins)
- **Sorting**: Most recent, most helpful reviews

## API Endpoints

### Review Management
- `POST /api/reviews` - Create a new review
- `PUT /api/reviews/{id}` - Update own review
- `DELETE /api/reviews/{id}?studentId={studentId}` - Delete own review
- `GET /api/reviews/{id}` - Get review by ID

### Query Reviews
- `GET /api/reviews/course/{courseId}` - Get all approved reviews for a course
- `GET /api/reviews/course/{courseId}/recent` - Get most recent reviews
- `GET /api/reviews/course/{courseId}/helpful` - Get most helpful reviews
- `GET /api/reviews/student/{studentId}` - Get all reviews by a student

### Rating Statistics
- `GET /api/reviews/course/{courseId}/stats` - Get rating stats (avg, counts by star)

### Instructor Responses
- `POST /api/reviews/responses` - Add instructor response to a review
- `PUT /api/reviews/responses/{id}?response={text}` - Update instructor response

### Review Helpfulness
- `POST /api/reviews/{reviewId}/helpful?userId={userId}&helpful={true/false}` - Mark review as helpful/not helpful

### Moderation (Admin)
- `POST /api/reviews/{reviewId}/moderate?status={APPROVED/REJECTED/FLAGGED}&moderatorId={id}` - Moderate review
- `GET /api/reviews/pending` - Get all pending reviews

## Technologies

- Spring Boot 3.3.4
- Spring Data JPA
- PostgreSQL
- Eureka Client (Service Discovery)
- Lombok
- Jakarta Validation

## Database

Create the database:
```sql
CREATE DATABASE review_service_db;
```

Tables are auto-created by Hibernate:
- `reviews` - Course reviews with ratings
- `instructor_responses` - Instructor replies to reviews
- `review_helpfulness` - User votes on review helpfulness

## Running the Service

```bash
cd review-service
mvn clean install
mvn spring-boot:run
```

Service runs on port **8090**.

## Example Workflow

1. **Student submits review:**
   ```json
   POST /api/reviews
   {
     "courseId": 1,
     "studentId": 42,
     "studentName": "John Doe",
     "rating": 5,
     "comment": "Excellent course! Learned a lot.",
     "verified": true
   }
   ```

2. **Get course rating stats:**
   ```
   GET /api/reviews/course/1/stats
   Response: {
     "averageRating": 4.7,
     "totalReviews": 150,
     "fiveStarCount": 100,
     "fourStarCount": 30,
     ...
   }
   ```

3. **Instructor responds:**
   ```json
   POST /api/reviews/responses
   {
     "reviewId": 1,
     "instructorId": 10,
     "instructorName": "Dr. Smith",
     "response": "Thank you for your feedback!"
   }
   ```

4. **Users vote helpfulness:**
   ```
   POST /api/reviews/1/helpful?userId=50&helpful=true
   ```

## Business Rules

- ✅ One review per student per course
- ✅ Only review author can update/delete their review
- ✅ Reviews auto-approved (can enable moderation)
- ✅ Rating must be 1-5 stars
- ✅ Comment max 2000 characters
- ✅ Users can change their helpfulness vote
- ✅ Instructor responses are one per review
