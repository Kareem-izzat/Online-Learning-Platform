# Video Service

The Video Service is responsible for managing video uploads, storage, and streaming for the Online Learning Platform. It handles video metadata, upload progress tracking, and provides endpoints for video retrieval and streaming.

## Features

- **Video Upload**: Upload videos with metadata (title, description, lesson association)
- **File Storage**: Store video files on the filesystem with unique identifiers
- **Video Streaming**: Stream videos to clients
- **Metadata Management**: Update video information
- **Upload Status Tracking**: Track upload progress and status (PENDING, UPLOADING, PROCESSING, COMPLETED, FAILED)
- **View Count Analytics**: Track video views
- **Lesson Association**: Link videos to course lessons
- **Uploader Tracking**: Track which instructor uploaded each video

## Technology Stack

- **Spring Boot 3.3.4**
- **Spring Data JPA**
- **PostgreSQL**
- **Spring Cloud Netflix Eureka Client**
- **Lombok**
- **Maven**

## Configuration

### Database
- Database Name: `video_service_db`
- Port: 5432
- Username: `postgres`
- Password: `123456`

### Service
- Port: 8084
- Service Name: `video-service`
- Eureka Server: http://localhost:8761/eureka/

### File Upload
- Max File Size: 500MB
- Max Request Size: 500MB
- Upload Directory: `uploads/videos`

## API Endpoints

### Upload Video
```
POST /api/videos/upload
Content-Type: multipart/form-data

Parameters:
- file: MultipartFile (required)
- title: String (required)
- description: String (optional)
- lessonId: Long (optional)
- uploadedBy: Long (required)

Response: 201 Created
{
  "id": 1,
  "lessonId": 5,
  "title": "Introduction to Java",
  "description": "This video covers Java basics",
  "videoUrl": "uploads/videos/uuid-filename.mp4",
  "thumbnailUrl": null,
  "durationSeconds": null,
  "fileSize": 52428800,
  "fileName": "intro-java.mp4",
  "contentType": "video/mp4",
  "uploadStatus": "COMPLETED",
  "uploadProgress": 100,
  "errorMessage": null,
  "viewsCount": 0,
  "uploadedBy": 1,
  "createdAt": "2025-10-21T10:30:00",
  "updatedAt": "2025-10-21T10:30:00",
  "publishedAt": "2025-10-21T10:30:00"
}
```

### Get Video by ID
```
GET /api/videos/{id}

Response: 200 OK
{
  "id": 1,
  "lessonId": 5,
  "title": "Introduction to Java",
  ...
}
```

### Get All Videos
```
GET /api/videos

Response: 200 OK
[
  {
    "id": 1,
    "title": "Introduction to Java",
    ...
  },
  {
    "id": 2,
    "title": "Advanced Spring Boot",
    ...
  }
]
```

### Get Videos by Lesson
```
GET /api/videos/lesson/{lessonId}

Response: 200 OK
[
  {
    "id": 1,
    "lessonId": 5,
    "title": "Introduction to Java",
    ...
  }
]
```

### Get Videos by Uploader
```
GET /api/videos/uploader/{uploaderId}

Response: 200 OK
[
  {
    "id": 1,
    "uploadedBy": 1,
    "title": "Introduction to Java",
    ...
  }
]
```

### Update Video Metadata
```
PUT /api/videos/{id}/metadata
Content-Type: application/json

{
  "title": "Updated Title",
  "description": "Updated description",
  "lessonId": 5
}

Response: 200 OK
{
  "id": 1,
  "title": "Updated Title",
  "description": "Updated description",
  ...
}
```

### Increment View Count
```
POST /api/videos/{id}/views

Response: 200 OK
```

### Delete Video
```
DELETE /api/videos/{id}

Response: 204 No Content
```

### Stream Video
```
GET /api/videos/{id}/stream

Response: 200 OK
Content-Type: video/mp4
Content-Disposition: inline; filename="intro-java.mp4"
[Binary video data]
```

## Upload Status States

- **PENDING**: Video upload initiated but not started
- **UPLOADING**: Video is being uploaded
- **PROCESSING**: Video is being processed (transcoding, thumbnail generation, etc.)
- **COMPLETED**: Video successfully uploaded and processed
- **FAILED**: Upload or processing failed

## Database Schema

### videos table
- `id`: BIGSERIAL PRIMARY KEY
- `lesson_id`: BIGINT (foreign key to lesson in course-service)
- `title`: VARCHAR(255) NOT NULL
- `description`: TEXT
- `video_url`: VARCHAR(500)
- `thumbnail_url`: VARCHAR(500)
- `duration_seconds`: INTEGER
- `file_size`: BIGINT
- `file_name`: VARCHAR(255)
- `content_type`: VARCHAR(100)
- `upload_status`: VARCHAR(50)
- `upload_progress`: INTEGER (0-100)
- `error_message`: TEXT
- `views_count`: BIGINT
- `uploaded_by`: BIGINT (foreign key to user in user-service)
- `created_at`: TIMESTAMP
- `updated_at`: TIMESTAMP
- `published_at`: TIMESTAMP

## Building and Running

### Prerequisites
1. PostgreSQL installed and running
2. Create database: `video_service_db`
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
java -jar target/video-service-1.0.0-SNAPSHOT.jar
```

## Testing with Postman

### Upload a Video
1. Create a POST request to `http://localhost:8084/api/videos/upload`
2. Select "form-data" in Body tab
3. Add key "file" and select type "File", then choose your video file
4. Add key "title" with value (e.g., "My Video")
5. Add key "uploadedBy" with value (e.g., 1)
6. (Optional) Add keys "description" and "lessonId"
7. Send the request

### Get Videos
- Get all videos: `GET http://localhost:8084/api/videos`
- Get specific video: `GET http://localhost:8084/api/videos/1`
- Get videos for lesson: `GET http://localhost:8084/api/videos/lesson/5`
- Get videos by uploader: `GET http://localhost:8084/api/videos/uploader/1`

### Stream Video
- Open in browser or use GET request: `http://localhost:8084/api/videos/1/stream`

## Integration with Other Services

- **Course Service**: Videos are associated with lessons via `lessonId`
- **User Service**: Videos track the uploader via `uploadedBy` (instructor ID)
- **API Gateway**: All endpoints accessible through gateway at `http://localhost:8080/video-service/api/videos`

## Future Enhancements

- Video transcoding to multiple resolutions
- Automatic thumbnail generation
- Video compression
- Cloud storage integration (AWS S3, Azure Blob Storage)
- Video chunking for resumable uploads
- HLS/DASH streaming support
- Video subtitles/captions support
- Video analytics and engagement metrics
