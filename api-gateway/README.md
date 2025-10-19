# API Gateway

API Gateway for the Online Learning Platform microservices architecture.

## Overview
This service acts as the single entry point for all client requests. It routes requests to appropriate microservices using service discovery.

## Configuration
- **Port:** 8080
- **Routes:** Auto-discovered from Eureka

## Features
- Dynamic routing based on Eureka service registry
- Load balancing across service instances
- Centralized entry point for all APIs
- Future: Authentication, rate limiting, circuit breaker

## How It Works
The gateway automatically creates routes for all services registered in Eureka:

```
http://localhost:8080/{service-name}/**
```

For example:
- `http://localhost:8080/user-service/api/users` → routes to User Service
- `http://localhost:8080/course-service/api/courses` → routes to Course Service

## Running the Service

### Using Maven
```bash
cd api-gateway
mvn spring-boot:run
```

### Using Spring Boot Dashboard
Click the play button next to `api-gateway` in VS Code's Spring Boot Dashboard.

## Testing
Once all services are running, test the gateway:

```bash
# Get all users through gateway
curl http://localhost:8080/user-service/api/users

# Create a user through gateway
curl -X POST http://localhost:8080/user-service/api/users \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123","firstName":"Test","lastName":"User","role":"STUDENT"}'
```

## Architecture
```
Client → API Gateway (8080) → Eureka Discovery (8761) → Microservices (8081, 8082, ...)
```
