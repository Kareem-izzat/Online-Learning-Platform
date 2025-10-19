# User Service - Quick Guide

## 📋 What's Been Created

### Structure:
```
user-service/
├── src/main/java/com/learningplatform/userservice/
│   ├── UserServiceApplication.java (Main app)
│   ├── controller/
│   │   └── UserController.java (REST endpoints)
│   ├── service/
│   │   └── UserService.java (Business logic)
│   ├── repository/
│   │   └── UserRepository.java (Database access)
│   ├── entity/
│   │   └── User.java (Database entity)
│   ├── dto/
│   │   ├── UserRequestDto.java (Input)
│   │   └── UserResponseDto.java (Output)
│   └── enums/
│       └── Role.java (STUDENT, INSTRUCTOR, ADMIN)
└── src/main/resources/
    └── application.properties (Configuration)
```

## 🎯 Features

### REST Endpoints:
- `POST /api/users` - Create new user
- `GET /api/users` - Get all users
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users/email/{email}` - Get user by email
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

### User Roles:
- **STUDENT** - Can enroll in courses
- **INSTRUCTOR** - Can create and manage courses
- **ADMIN** - Full system access

## 🗄️ Database

**Database Name:** `user_service_db`  
**Port:** 5432 (PostgreSQL)  
**Username:** postgres  
**Password:** postgres

### Create Database:
```sql
CREATE DATABASE user_service_db;
```

## 🚀 Run the Service

```bash
cd user-service
mvn spring-boot:run
```

Service will start on **http://localhost:8081**

## 📝 Test the API

### Create a Student:
```bash
curl -X POST http://localhost:8081/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "student@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe",
    "role": "STUDENT"
  }'
```

### Create an Instructor:
```bash
curl -X POST http://localhost:8081/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "instructor@example.com",
    "password": "password123",
    "firstName": "Jane",
    "lastName": "Smith",
    "role": "INSTRUCTOR"
  }'
```

### Get All Users:
```bash
curl http://localhost:8081/api/users
```

### Get User by ID:
```bash
curl http://localhost:8081/api/users/1
```

## ✅ Status
- [x] User entity with roles
- [x] CRUD operations
- [x] Input validation
- [x] PostgreSQL integration
- [x] REST API endpoints
- [ ] Password hashing (TODO)
- [ ] JWT authentication (TODO)

## 🔜 Next Steps
1. Create Service Discovery (Eureka)
2. Create API Gateway
3. Add JWT authentication
4. Build Course Service
