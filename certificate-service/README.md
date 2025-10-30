# Certificate Service

## Overview
Certificate Service generates and manages PDF certificates for students who complete courses. Features include certificate generation, verification, download tracking, and analytics.

## Features
- **Certificate Generation**: Creates professional PDF certificates with course completion details
- **Verification System**: Unique verification codes for certificate authenticity
- **Download Tracking**: Tracks certificate downloads and statistics
- **Certificate Management**: Revoke, suspend, or expire certificates
- **Analytics**: Student and course certificate statistics
- **Template Support**: Configurable certificate templates

## Technologies
- Spring Boot 3.3.4
- Spring Data JPA
- PostgreSQL
- iText 7 (PDF Generation)
- Eureka Discovery Client

## Port
8091

## Database
certificate_service_db

## API Endpoints

### Certificate Generation
- `POST /api/certificates` - Generate a new certificate
- `GET /api/certificates/{id}` - Get certificate by ID
- `GET /api/certificates/number/{certificateNumber}` - Get certificate by certificate number
- `GET /api/certificates/{id}/download` - Download certificate PDF

### Verification
- `GET /api/certificates/verify/{verificationCode}` - Verify certificate authenticity

### Certificate Management
- `GET /api/certificates/student/{studentId}` - Get all certificates for a student
- `GET /api/certificates/course/{courseId}` - Get all certificates for a course
- `PUT /api/certificates/{id}/revoke` - Revoke a certificate

### Analytics
- `GET /api/certificates/student/{studentId}/stats` - Get student certificate statistics
- `GET /api/certificates/course/{courseId}/stats` - Get course certificate statistics

## Certificate Features
- Unique certificate number
- Unique verification code
- Student information
- Course details
- Completion and issue dates
- Final grade
- Course duration
- Instructor name
- QR code for verification (future)
- Digital signature (future)

## Configuration
```properties
certificate.storage.path=./certificates
certificate.template.path=./templates
certificate.issuer.name=LearnIT Online Learning Platform
certificate.issuer.signature=Dr. John Smith, CEO
certificate.verification.url=http://localhost:8091/api/certificates/verify/
```
