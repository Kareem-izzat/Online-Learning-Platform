# Online Learning Platform

A microservices-based online learning platform built with Spring Boot.

## 🎓 Project Overview

Platform where:
- **Instructors** create and publish courses
- **Students** browse, purchase, and complete courses  
- **System** handles payments, tracks progress, and sends notifications

## 🏗️ Architecture

This project uses microservices architecture with:
- Service Discovery (Eureka)
- API Gateway
- Event-Driven Communication (Kafka)
- Independent databases per service

## 📦 Microservices

### ✅ Implemented
1. **User Service** (Port 8081)
   - User authentication and management
   - Role-based access (STUDENT, INSTRUCTOR, ADMIN)
   - PostgreSQL database
   - Full CRUD REST API

2. **Discovery Service** (Port 8761)
   - Eureka service registry
   - Service discovery and health monitoring
   - Dashboard at http://localhost:8761

### 🚧 Planned
3. **API Gateway** - Single entry point for all services
4. **Course Service** - Course creation and management
5. **Enrollment Service** - Student enrollments and progress
6. **Video Service** - Video streaming and tracking
7. **Assignment Service** - Homework and grading
8. **Notification Service** - Email and in-app notifications
9. **Payment Service** - Course purchases

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL 15+
- Git

### Build All Services
```bash
mvn clean install
```

### Run Discovery Service
```bash
cd discovery-service
mvn spring-boot:run
```
Access Eureka Dashboard: http://localhost:8761

### Run User Service
```bash
cd user-service
mvn spring-boot:run
```
API available at: http://localhost:8081/api/users

## 📝 Status
🟢 **User Service** - Operational  
🟢 **Discovery Service** - Operational  
🔄 **API Gateway** - In Development
