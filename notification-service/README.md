# Notification Service

Email and in-app notification management service for the Online Learning Platform.

## Features

- **In-App Notifications**: Store and manage notifications in database
- **Email Notifications**: Send emails using JavaMailSender
- **HTML Email Templates**: Beautiful Thymeleaf templates for various notification types
- **Async Processing**: Non-blocking email sending with @Async
- **User Preferences**: Users can control which notifications they receive
- **Notification Categories**: USER, COURSE, ENROLLMENT, ASSIGNMENT, VIDEO, PAYMENT, SYSTEM
- **Notification Types**: INFO, SUCCESS, WARNING, ERROR
- **Read/Unread Tracking**: Mark notifications as read, get unread count
- **Auto-cleanup**: Delete old read notifications

## Tech Stack

- Spring Boot 3.3.4
- Spring Data JPA
- PostgreSQL
- Spring Mail
- Thymeleaf Template Engine
- Eureka Client
- Lombok
- Jakarta Validation

## Configuration

### Email Setup (Gmail Example)

1. Enable 2-Factor Authentication on your Gmail account
2. Generate an App Password: https://myaccount.google.com/apppasswords
3. Update `application.properties`:

```properties
spring.mail.username=your-email@gmail.com
spring.mail.password=your-16-digit-app-password
```

### Database Setup

Create PostgreSQL database:

```sql
CREATE DATABASE notification_service_db;
```

Tables will be auto-created by Hibernate.

## API Endpoints

### Notification Endpoints

#### Create Notification
```http
POST /api/notifications
Content-Type: application/json

{
  "userId": 5,
  "title": "Assignment Graded",
  "message": "Your Java Quiz has been graded. You scored 85/100!",
  "type": "SUCCESS",
  "category": "ASSIGNMENT",
  "referenceId": "1",
  "actionUrl": "/assignments/1",
  "sendEmail": true
}
```

#### Get All Notifications for User
```http
GET /api/notifications/user/5
```

#### Get Unread Notifications
```http
GET /api/notifications/user/5/unread
```

#### Get Unread Count
```http
GET /api/notifications/user/5/unread-count
```

#### Get Notifications by Category
```http
GET /api/notifications/user/5/category/ASSIGNMENT
```

#### Get Recent Notifications (last 7 days)
```http
GET /api/notifications/user/5/recent?days=7
```

#### Mark as Read
```http
PUT /api/notifications/1/read
```

#### Mark All as Read
```http
PUT /api/notifications/user/5/read-all
```

#### Delete Notification
```http
DELETE /api/notifications/1
```

#### Delete All Read Notifications
```http
DELETE /api/notifications/user/5/read
```

---

### Email Endpoints

#### Send Simple Email
```http
POST /api/email/simple?to=student@example.com&subject=Test&text=Hello
```

#### Send Welcome Email
```http
POST /api/email/welcome?toEmail=student@example.com&userName=John Doe
```

#### Send Enrollment Email
```http
POST /api/email/enrollment?toEmail=student@example.com&userName=John Doe&courseName=Java Programming
```

#### Send Assignment Reminder
```http
POST /api/email/assignment-reminder?toEmail=student@example.com&userName=John&assignmentTitle=Java Quiz&dueDate=2025-10-30
```

#### Send Assignment Graded
```http
POST /api/email/assignment-graded?toEmail=student@example.com&userName=John&assignmentTitle=Java Quiz&score=85
```

---

### Notification Preference Endpoints

#### Get User Preferences
```http
GET /api/notification-preferences/user/5
```

#### Get Specific Preference
```http
GET /api/notification-preferences/user/5/category/ASSIGNMENT
```

#### Update Preference
```http
PUT /api/notification-preferences/user/5/category/ASSIGNMENT?emailEnabled=false&inAppEnabled=true
```

#### Initialize Default Preferences
```http
POST /api/notification-preferences/user/5/initialize
```

#### Disable All Email Notifications
```http
PUT /api/notification-preferences/user/5/disable-all-email
```

#### Enable All Email Notifications
```http
PUT /api/notification-preferences/user/5/enable-all-email
```

## Database Schema

### notifications Table
```sql
- id (PK)
- user_id
- title
- message
- type (INFO, SUCCESS, WARNING, ERROR)
- category (USER, COURSE, ENROLLMENT, ASSIGNMENT, VIDEO, PAYMENT, SYSTEM)
- is_read
- email_sent
- reference_id
- action_url
- created_at
- read_at
```

### notification_preferences Table
```sql
- id (PK)
- user_id
- category
- email_enabled
- in_app_enabled
- sms_enabled
- created_at
- updated_at
- UNIQUE(user_id, category)
```

## Email Templates

Available Thymeleaf templates:

- **welcome.html** - Welcome email for new users
- **enrollment.html** - Enrollment confirmation
- **assignment-reminder.html** - Assignment deadline reminder
- **assignment-graded.html** - Assignment grading notification

### Template Variables

**welcome.html:**
- `userName`

**enrollment.html:**
- `userName`
- `courseName`

**assignment-reminder.html:**
- `userName`
- `assignmentTitle`
- `dueDate`

**assignment-graded.html:**
- `userName`
- `assignmentTitle`
- `score`

## Building and Running

### Build
```bash
cd notification-service
mvn clean install
```

### Run
```bash
mvn spring-boot:run
```

The service will start on **port 8086**.

## Testing with Postman

1. **Create a notification:**
   - POST to `http://localhost:8086/api/notifications`
   - Set `sendEmail: true` to also send email

2. **Check unread count:**
   - GET `http://localhost:8086/api/notifications/user/5/unread-count`

3. **Send welcome email:**
   - POST to `http://localhost:8086/api/email/welcome`

4. **Update preferences:**
   - PUT to disable email for assignments

## Integration with Other Services

### User Service
- Fetch user email addresses for sending emails
- Call when creating notification: `GET /api/users/{id}`

### Assignment Service
- Send notification when assignment is created
- Send reminder when deadline is approaching
- Send notification when assignment is graded

### Enrollment Service
- Send confirmation email when student enrolls
- Send welcome email with course details

### Course Service
- Notify enrolled students when new content is added
- Send course completion notifications

## Business Logic

### Notification Preferences
- Default: All email and in-app notifications enabled
- Users can disable per category (ASSIGNMENT, COURSE, etc.)
- If disabled, notification is not created/sent

### Async Email Sending
- Emails sent asynchronously using @Async
- Non-blocking - doesn't delay API response
- Failed emails logged but don't throw exceptions

### Auto-cleanup
- Endpoint to delete read notifications older than 30 days
- Helps manage database size
- Keeps unread notifications indefinitely

## Future Enhancements

- [ ] SMS notifications using Twilio
- [ ] Push notifications (Firebase)
- [ ] WebSocket for real-time notifications
- [ ] Notification scheduling (send at specific time)
- [ ] Batch email sending
- [ ] Email delivery tracking
- [ ] Rich text editor for custom templates
- [ ] Notification analytics
- [ ] Multi-language support
- [ ] Email unsubscribe links

## Port Information

- **Notification Service**: 8086
- **Eureka Server**: 8761
- **API Gateway**: 8080

## Dependencies

- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- Spring Boot Starter Mail
- Spring Boot Starter Thymeleaf
- PostgreSQL Driver
- Eureka Client
- Lombok
- Validation

## Author

Online Learning Platform Development Team

## License

Proprietary - All Rights Reserved
