# Online Learning Platform

A production-ready microservices-based online learning platform built with Spring Boot and Spring Cloud.

## 🎓 Project Overview

Enterprise-grade learning management system where:
- **Instructors** create and publish courses with videos, quizzes, and assignments
- **Students** browse, enroll, learn, and track progress
- **Admins** manage platform, users, and content
- **System** handles authentication, payments, notifications, and real-time analytics

## 🏗️ Architecture

Modern microservices architecture with:
- **Service Discovery**: Eureka for dynamic service registration
- **API Gateway**: Centralized authentication and routing
- **Event-Driven**: Kafka for asynchronous communication and analytics
- **Security**: JWT-based authentication with BCrypt password hashing
- **Database per Service**: PostgreSQL for data isolation
- **Transactional Outbox**: Guaranteed event delivery
- **RESTful APIs**: Comprehensive endpoints for all services

## 📦 Microservices

### Core Services
1. **Discovery Service** (Port 8761)
   - Eureka service registry and health monitoring
   - Dashboard: http://localhost:8761

2. **API Gateway** (Port 8080) ✅ **SECURED**
   - Single entry point for all services
   - JWT authentication and authorization
   - Route management and load balancing
   - CORS configuration

3. **User Service** (Port 8081) ✅ **SECURED**
   - User registration and authentication
   - JWT token generation
   - BCrypt password hashing
   - Role-based access control (STUDENT, INSTRUCTOR, ADMIN)
   - Endpoints: `/api/auth/register`, `/api/auth/login`, `/api/users/**`

### Business Services
4. **Course Service** (Port 8082)
   - Course creation, publishing, and management
   - Modules and lessons organization
   - Course catalog and search

5. **Enrollment Service** (Port 8083)
   - Student enrollments
   - Enrollment status tracking
   - Course access management

6. **Video Service** (Port 8084)
   - Video upload and streaming
   - Video metadata management
   - Progress tracking

7. **Quiz Service** (Port 8085)
   - Quiz creation and management
   - Multiple choice, true/false questions
   - Quiz attempts and grading

8. **Assignment Service** (Port 8086)
   - Assignment creation and submission
   - File uploads and downloads
   - Grading and feedback

9. **Progress Service** (Port 8087)
   - Course completion tracking
   - Lesson, quiz, and assignment progress
   - Student performance analytics

10. **Payment Service** (Port 8088)
    - Course purchases
    - Payment processing
    - Transaction history and refunds

11. **Notification Service** (Port 8089)
    - Email notifications
    - In-app notifications
    - User preferences management

12. **Certificate Service** (Port 8090)
    - Certificate generation
    - PDF creation and downloads
    - Certificate verification

13. **Review Service** (Port 8091)
    - Course reviews and ratings
    - Instructor responses
    - Review moderation

14. **Discussion Service** (Port 8092) ✅ **WITH OUTBOX PATTERN**
    - Discussion threads and comments
    - Voting system (upvote/downvote)
    - Real-time notifications via Kafka
    - Transactional outbox for guaranteed event delivery

15. **Analytics Service** (Port 8100) ✅ **WITH KAFKA INTEGRATION**
    - Real-time event processing
    - Discussion thread analytics
    - HTTP and Kafka ingestion modes
    - Idempotency for exactly-once processing

## � Security

**Centralized Authentication at API Gateway**

All microservices are protected by JWT-based authentication at the API Gateway level:

- **Single Point of Validation**: JWT tokens validated once at gateway
- **User Context Propagation**: Gateway adds `X-User-Id` and `X-Username` headers
- **BCrypt Password Hashing**: Secure password storage
- **Public Endpoints**: Login, register, browse courses (GET requests)
- **Protected Endpoints**: All POST/PUT/DELETE operations, user-specific data

### Quick Authentication Flow
```bash
# 1. Register a user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"pass123","firstName":"John","lastName":"Doe","role":"STUDENT"}'

# 2. Login to get JWT token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"pass123"}'

# 3. Use token in requests
curl http://localhost:8080/api/users/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

📖 **Full Documentation**: See [API-GATEWAY-SECURITY.md](API-GATEWAY-SECURITY.md)

## �🚀 Getting Started

### Prerequisites
- **Java 17+**
- **Maven 3.8+**
- **PostgreSQL 15+**
- **Apache Kafka 3.7.0** (for Analytics and Discussion services)
- **Docker** (optional, for Kafka via docker-compose)

### Quick Start

#### 1. Build All Services
```bash
mvn clean install -DskipTests
```

#### 2. Start Core Services
```bash
# Terminal 1 - Discovery Service
cd discovery-service
mvn spring-boot:run

# Terminal 2 - API Gateway (with authentication)
cd api-gateway
mvn spring-boot:run

# Terminal 3 - User Service (authentication provider)
cd user-service
mvn spring-boot:run
```

**Access Points**:
- Eureka Dashboard: http://localhost:8761
- API Gateway: http://localhost:8080
- User Service: http://localhost:8081

#### 3. Start Kafka (for Analytics and Discussion)
```bash
cd analytics
docker-compose up -d
```

#### 4. Start Business Services
```bash
# Terminal 4 - Discussion Service (with Outbox pattern)
cd discussion-service
mvn spring-boot:run

# Terminal 5 - Analytics Service (Kafka consumer)
cd analytics
mvn spring-boot:run

# Add other services as needed...
```

### Test Authentication
```bash
# Automated test script
.\test-auth.ps1
```

Tests: register → login → JWT validation → protected access → error handling

## 📚 Key Features

### ✅ Implemented

#### Authentication & Security
- JWT-based authentication at API Gateway
- BCrypt password hashing
- User registration and login
- Role-based access control (STUDENT, INSTRUCTOR, ADMIN)
- Centralized security (single validation point)

#### Event-Driven Architecture
- **Kafka Integration**: Real-time event streaming
- **Outbox Pattern**: Transactional event publishing in Discussion Service
- **Analytics Service**: HTTP and Kafka event ingestion with idempotency
- **Guaranteed Delivery**: No event loss even on failures

#### Microservices Infrastructure
- **Service Discovery**: Automatic service registration with Eureka
- **API Gateway**: Centralized routing, authentication, and CORS
- **Database per Service**: Complete data isolation
- **RESTful APIs**: Comprehensive CRUD operations

#### Real-Time Analytics
- Thread creation, comment, vote events
- Aggregate statistics (comment count, vote scores)
- Duplicate event detection (idempotency)
- Batch event ingestion support

### Development Tools
- **Test Scripts**: PowerShell scripts for testing auth, Kafka, and outbox
- **Docker Compose**: Kafka setup with KRaft mode
- **Comprehensive Documentation**: 2000+ lines across multiple MD files

## � Documentation

- **[API-GATEWAY-SECURITY.md](API-GATEWAY-SECURITY.md)** - Complete security architecture and configuration
- **[GATEWAY-SECURITY-IMPLEMENTATION.md](GATEWAY-SECURITY-IMPLEMENTATION.md)** - Implementation summary and quick reference
- **[SECURITY.md](SECURITY.md)** - Overall security guide
- **[END-TO-END-ARCHITECTURE.md](END-TO-END-ARCHITECTURE.md)** - Complete system architecture
- **[DEPLOYMENT.md](DEPLOYMENT.md)** - Production deployment guide
- **[analytics/INTEGRATION-PATTERNS.md](analytics/INTEGRATION-PATTERNS.md)** - Event-driven patterns
- **[discussion-service/OUTBOX-PATTERN.md](discussion-service/OUTBOX-PATTERN.md)** - Transactional outbox implementation

## 🧪 Testing

### Test Scripts
```bash
# Test authentication flow
.\test-auth.ps1

# Test Kafka integration
cd analytics
.\test-kafka.ps1

# Test outbox pattern
cd discussion-service
.\test-outbox.ps1
```

### Manual API Testing
See individual service READMEs and [API-GATEWAY-SECURITY.md](API-GATEWAY-SECURITY.md) for endpoint documentation.

## 🏗️ Project Structure

```
Online-Learning-Platform/
├── discovery-service/       # Eureka service registry
├── api-gateway/            # Gateway with JWT authentication
├── user-service/           # User management and authentication
├── course-service/         # Course management
├── enrollment-service/     # Student enrollments
├── video-service/          # Video streaming
├── quiz-service/           # Quiz management
├── assignment-service/     # Assignment management
├── progress-service/       # Progress tracking
├── payment-service/        # Payment processing
├── notification-service/   # Notifications
├── certificate-service/    # Certificate generation
├── review-service/         # Course reviews
├── discussion-service/     # Discussions with Outbox pattern
├── analytics/              # Analytics with Kafka integration
└── docs/                   # Documentation
```

## � Configuration

### Important Configuration Files

**JWT Secret** (must match in both services):
- `api-gateway/src/main/resources/application.properties`
- `user-service/src/main/resources/application.properties`

```properties
jwt.secret=your-256-bit-secret-change-this-in-production-make-it-at-least-32-characters-long
jwt.expiration=86400000  # 24 hours
```

**⚠️ Production**: Change JWT secret and use environment variables!

### Database Configuration
Each service uses PostgreSQL. Update in `application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/[service]_db
spring.datasource.username=postgres
spring.datasource.password=your_password
```

## 📊 Technology Stack

- **Backend**: Spring Boot 3.3.4, Spring Cloud 2023.0.3
- **Security**: Spring Security, JWT (jjwt 0.12.3), BCrypt
- **Service Discovery**: Netflix Eureka
- **API Gateway**: Spring Cloud Gateway
- **Messaging**: Apache Kafka 3.7.0 (KRaft mode)
- **Database**: PostgreSQL 15
- **Build Tool**: Maven 3.8+
- **Java Version**: 17+
- **Containerization**: Docker & Docker Compose

## 🚀 Production Deployment

See [DEPLOYMENT.md](DEPLOYMENT.md) for:
- Docker containerization
- Kubernetes deployment
- Environment configuration
- SSL/TLS setup
- Monitoring and logging
- Backup strategies

## 📝 Status

| Service | Status | Features |
|---------|--------|----------|
| Discovery Service | � Operational | Service registry, health monitoring |
| API Gateway | 🟢 Secured | JWT auth, routing, CORS |
| User Service | 🟢 Secured | Registration, login, BCrypt passwords |
| Discussion Service | 🟢 Operational | Threads, comments, Outbox pattern |
| Analytics Service | 🟢 Operational | Kafka consumer, HTTP API, idempotency |
| Course Service | 🟡 Basic CRUD | Courses, modules, lessons |
| Enrollment Service | 🟡 Basic CRUD | Student enrollments |
| Video Service | 🟡 Basic CRUD | Video metadata, file uploads |
| Quiz Service | 🟡 Basic CRUD | Quizzes, questions, attempts |
| Assignment Service | 🟡 Basic CRUD | Assignments, submissions, grading |
| Progress Service | 🟡 Basic CRUD | Course and lesson progress |
| Payment Service | 🟡 Basic CRUD | Payments, refunds |
| Notification Service | 🟡 Basic CRUD | Email and in-app notifications |
| Certificate Service | 🟡 Basic CRUD | Certificate generation |
| Review Service | 🟡 Basic CRUD | Course reviews and ratings |

**Legend**: 🟢 Production-ready | 🟡 Functional (needs security integration) | 🔴 In Development

## 🤝 Contributing

This is a demonstration project showcasing microservices architecture, event-driven design, and centralized security patterns.

## 📄 License

This project is open source and available for educational purposes.

---

**Built with ❤️ using Spring Boot and Spring Cloud**
