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

📖 **Full Documentation**: See [SECURITY.md](SECURITY.md)

## 🚀 Getting Started

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

## 🐳 Docker (Recommended for Local Development)

The repo includes production-ready Dockerfiles and a Docker Compose setup for easy local development.

### What's Included

**Core Stack** (`docker-compose.yml`):
- PostgreSQL 15 (with persistent volume)
- Discovery Service (Eureka)
- User Service (with JWT authentication)
- API Gateway (with JWT validation)

**Separate Stack** (`analytics/docker-compose.yml`):
- Apache Kafka (KRaft mode)
- For Analytics and Discussion services

### Quick Start

#### 1. Configure Environment Variables

Copy the example environment file and customize it:

```powershell
# Copy .env.example to .env
copy .env.example .env

# Edit .env and set:
# - Secure database password
# - Strong JWT secret (generate with: openssl rand -base64 32)
# - Custom ports if needed
```

#### 2. Build and Start Services

```powershell
# From repo root - build and start all services
docker compose up --build -d

# Wait for services to be healthy (30-60 seconds)
docker compose ps
```

**Services will be available at**:
- Eureka Dashboard: http://localhost:8761
- API Gateway: http://localhost:8080
- User Service: http://localhost:8081 (via Gateway)
- PostgreSQL: localhost:5432

#### 3. Test Authentication

```powershell
# Register a new user
curl -X POST http://localhost:8080/api/auth/register `
  -H "Content-Type: application/json" `
  -d '{\"email\":\"test@example.com\",\"password\":\"pass123\",\"firstName\":\"John\",\"lastName\":\"Doe\",\"role\":\"STUDENT\"}'

# Login to get JWT token
curl -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{\"email\":\"test@example.com\",\"password\":\"pass123\"}'
```

#### 4. View Logs

```powershell
# All services
docker compose logs -f

# Specific service
docker compose logs -f api-gateway
docker compose logs -f user-service
```

#### 5. Stop Services

```powershell
# Stop containers (keeps volumes)
docker compose down

# Stop and remove volumes (clean slate)
docker compose down -v
```

### Advanced Usage

#### Start Kafka for Analytics

```powershell
# In separate terminal
cd analytics
docker compose up -d

# Kafka will be available at localhost:9092
```

#### Rebuild Single Service

```powershell
# Rebuild and restart a specific service
docker compose up --build -d api-gateway
```

#### Execute Commands Inside Containers

```powershell
# Connect to PostgreSQL
docker compose exec postgres psql -U postgres -d user_service_db

# Check Java version in service
docker compose exec user-service java -version
```

#### Resource Management

The Dockerfiles include:
- ✅ Multi-stage builds (smaller images)
- ✅ Non-root users (security)
- ✅ Healthchecks (proper startup ordering)
- ✅ JVM memory limits via `JAVA_OPTS`

Customize resources in `.env`:
```properties
JAVA_OPTS=-Xmx512m -Xms256m -XX:+UseG1GC
```

### Production Considerations

Before deploying to production:

1. **Security**:
   - Generate strong JWT secret: `openssl rand -base64 32`
   - Use secrets manager (AWS Secrets Manager, HashiCorp Vault)
   - Enable HTTPS with proper certificates
   - Set secure database passwords

2. **Scaling**:
   - Deploy to Kubernetes or Docker Swarm
   - Use external PostgreSQL (AWS RDS, Azure Database)
   - Configure horizontal pod autoscaling
   - Add load balancers

3. **Monitoring**:
   - Add Prometheus + Grafana for metrics
   - Configure centralized logging (ELK stack)
   - Set up alerting (PagerDuty, Slack)
   - Enable distributed tracing (Zipkin, Jaeger)

4. **Networking**:
   - Use overlay networks for service mesh
   - Configure proper ingress controllers
   - Set network policies for isolation
   - Enable TLS between services

See [DOCKER.md](DOCKER.md) for complete Docker setup guide with troubleshooting, production deployment, and Kubernetes migration.

### Troubleshooting

**Services won't start?**
```powershell
# Check service status
docker compose ps

# View detailed logs
docker compose logs

# Restart specific service
docker compose restart user-service
```

**Database connection errors?**
```powershell
# Ensure Postgres is healthy
docker compose exec postgres pg_isready

# Check connection from service
docker compose exec user-service ping postgres
```

**Port conflicts?**
```powershell
# Change ports in .env file
POSTGRES_PORT=5433
API_GATEWAY_PORT=8081
```

**Clean start needed?**
```powershell
# Remove everything and start fresh
docker compose down -v
docker compose up --build -d
```

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
- **Docker Support**: Production-ready Dockerfiles with multi-stage builds
- **Docker Compose**: Complete stack orchestration with healthchecks
- **Comprehensive Documentation**: Complete guides for security, Docker, and deployment

## 📖 Documentation

- **[README.md](README.md)** - This file (project overview and quick start)
- **[SECURITY.md](SECURITY.md)** - Complete security architecture and authentication guide
- **[DOCKER.md](DOCKER.md)** - Docker setup, production deployment, and Kubernetes migration
- **[analytics/INTEGRATION-PATTERNS.md](analytics/INTEGRATION-PATTERNS.md)** - Event-driven patterns (HTTP vs Kafka)
- **[analytics/event-contract.md](analytics/event-contract.md)** - Event schema definitions
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
See individual service READMEs and [SECURITY.md](SECURITY.md) for endpoint documentation and authentication examples.

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

See [DOCKER.md](DOCKER.md) for:
- Complete Docker setup guide
- Production-ready Dockerfiles
- Environment configuration
- Kubernetes migration with Kompose
- CI/CD pipeline examples
- Monitoring with Prometheus/Grafana
- High availability with Docker Swarm
- Troubleshooting guide

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

**Legend**: 🟢 Production-ready with full security | 🟡 Functional (secured via API Gateway) | 🔴 In Development

**Note**: All services (🟡) are secured through the API Gateway's centralized JWT authentication. They trust the `X-User-Id` header added by the gateway.

## 🤝 Contributing

This is a demonstration project showcasing microservices architecture, event-driven design, and centralized security patterns.

## 📄 License

This project is open source and available for educational purposes.

---

**Built with ❤️ using Spring Boot and Spring Cloud**
