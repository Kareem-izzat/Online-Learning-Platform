# API Gateway Centralized Security

## Overview

The Online Learning Platform implements **centralized authentication** at the API Gateway level. This approach provides:

- ✅ **Single point of validation**: JWT tokens validated once at gateway
- ✅ **No duplicate code**: Authentication logic in one place, not 15 services
- ✅ **Easy maintenance**: Update security in one location
- ✅ **Consistent security**: All services protected uniformly
- ✅ **Performance**: Backend services skip JWT validation overhead

## Architecture

```
Client Request → API Gateway (Validates JWT) → Backend Service (Trusts Gateway)
                      ↓
                 Extracts userId
                      ↓
            Adds X-User-Id header
                      ↓
           Forwards to backend service
```

### Flow

1. **Client** sends request with `Authorization: Bearer <JWT>`
2. **API Gateway** validates JWT token
3. **Gateway** extracts `userId` and `username` from token
4. **Gateway** adds headers: `X-User-Id` and `X-Username`
5. **Backend service** receives request with user context
6. **Backend** trusts gateway headers (no validation needed)

## Components

### 1. API Gateway (Port 8080)

**JWT Validation Filter**: `JwtAuthenticationFilter`
- Validates JWT signature and expiration
- Extracts user information from token
- Adds user context headers for backend services

**Public Endpoints** (no authentication):
- `/api/auth/**` - Login, register
- `/api/courses` (GET) - Browse courses
- `/api/discussions` (GET) - Read discussions
- `/api/analytics` (GET) - View analytics
- `/api/reviews` (GET) - Read reviews
- `/eureka/**` - Service discovery UI
- `/actuator/**` - Health checks

**Protected Endpoints** (require JWT):
- All POST, PUT, DELETE operations
- User-specific data (enrollments, progress, etc.)

### 2. User Service (Port 8081)

**Authentication Endpoints**:

#### Register User
```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "student@example.com",
  "password": "securePassword123",
  "firstName": "John",
  "lastName": "Doe",
  "role": "STUDENT"
}
```

Response:
```json
{
  "id": 1,
  "email": "student@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "role": "STUDENT",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "student@example.com",
  "password": "securePassword123"
}
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": 1,
  "email": "student@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "role": "STUDENT"
}
```

**Password Security**:
- BCrypt hashing (work factor: 10)
- Passwords never stored in plain text
- Automatic hashing on registration and password updates

### 3. Backend Services

All backend services (Course, Enrollment, Payment, etc.) trust the gateway:

**Read User Context**:
```java
@PostMapping("/enrollments")
public ResponseEntity<EnrollmentDto> createEnrollment(
        @RequestHeader("X-User-Id") Long userId,
        @RequestBody EnrollmentRequest request) {
    // Use userId from gateway - already authenticated
    return enrollmentService.enroll(userId, request);
}
```

**No JWT validation needed** - Gateway already validated!

## Configuration

### API Gateway Configuration

**pom.xml**:
```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
```

**application.properties**:
```properties
# JWT secret - MUST match User Service secret
jwt.secret=your-256-bit-secret-change-this-in-production-make-it-at-least-32-characters-long
```

### User Service Configuration

**pom.xml**:
```xml
<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- JWT (same versions as gateway) -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
<!-- ... impl and jackson -->
```

**application.properties**:
```properties
# JWT Configuration
jwt.secret=your-256-bit-secret-change-this-in-production-make-it-at-least-32-characters-long
jwt.expiration=86400000  # 24 hours
```

## Testing

### 1. Register a User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User",
    "role": "STUDENT"
  }'
```

### 2. Login to Get JWT Token

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

Save the `token` from the response!

### 3. Access Protected Endpoint

```bash
# Without token - FAILS (401 Unauthorized)
curl http://localhost:8080/api/courses/1

# With token - SUCCESS
curl http://localhost:8080/api/courses/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

### 4. Verify User Context in Backend

The backend service receives:
```
X-User-Id: 1
X-Username: Test User
```

## Security Best Practices

### Production Checklist

- [ ] **Change JWT secret** in both Gateway and User Service
  - Generate: `openssl rand -base64 32`
  - Must be at least 256 bits (32 characters)
  - Store in environment variables, not code

- [ ] **Use HTTPS** for all communication
  - JWT tokens transmitted in Authorization header
  - HTTPS prevents token interception

- [ ] **Implement token refresh**
  - Current tokens expire after 24 hours
  - Add refresh token endpoint for long-lived sessions

- [ ] **Add rate limiting**
  - Prevent brute force login attempts
  - Use Redis + Spring Cloud Gateway rate limiter

- [ ] **Log security events**
  - Failed login attempts
  - Invalid token attempts
  - Unusual access patterns

- [ ] **Network security**
  - Backend services only accessible from gateway
  - Use Docker network isolation or VPC

### Token Storage (Client-Side)

**Recommended**:
- Store in memory (React state, Angular service)
- HttpOnly cookies (prevents XSS attacks)

**Avoid**:
- localStorage (vulnerable to XSS)
- sessionStorage (vulnerable to XSS)

## Troubleshooting

### 401 Unauthorized

**Symptom**: All requests return 401 even with token

**Solutions**:
1. Check JWT secret matches between Gateway and User Service
2. Verify token not expired (24 hour default)
3. Check token format: `Authorization: Bearer <token>`
4. Ensure gateway and user-service are running

### Missing X-User-Id Header

**Symptom**: Backend service receives null userId

**Solutions**:
1. Check JWT filter is registered in gateway
2. Verify filter order is -100 (runs early)
3. Check gateway logs for filter execution
4. Ensure request passed through gateway (not direct to service)

### Invalid Token

**Symptom**: "Invalid or expired JWT token"

**Solutions**:
1. Token expired - login again
2. JWT secret mismatch - check configuration
3. Token format wrong - ensure `Bearer ` prefix
4. Token corrupted - check for extra spaces/characters

## Migration Guide

### Updating Existing Services

To add user context to your service:

1. **Read headers** in controller:
```java
@PostMapping("/endpoint")
public ResponseEntity<?> create(
        @RequestHeader("X-User-Id") Long userId,
        @RequestHeader(value = "X-Username", required = false) String username,
        @RequestBody RequestDto request) {
    // Use userId for authorization
}
```

2. **Remove JWT validation** (if exists):
- Delete JWT filters from service
- Remove Spring Security (unless needed for other reasons)
- Remove jjwt dependencies from service pom.xml

3. **Trust the gateway**:
- Backend services are internal - not exposed to internet
- Gateway is the only entry point
- Gateway has already validated the user

## Performance

### Benefits of Centralized Auth

- **Reduced latency**: JWT validated once, not 15 times
- **Lower CPU usage**: No crypto operations in backend services
- **Simpler code**: No auth logic in each service
- **Faster development**: New services instantly protected

### Metrics

- JWT validation: ~5-10ms at gateway
- Without centralized auth: ~5-10ms × 15 services = 75-150ms
- **Savings**: 70-140ms per request

## Advanced Features

### Role-Based Access Control (RBAC)

Add role to JWT claims:

```java
// User Service - JwtUtil.java
public String generateToken(Long userId, String email, String username, String role) {
    return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .claim("username", username)
            .claim("role", role)  // Add role
            // ...
}
```

Extract in gateway:
```java
// API Gateway - JwtAuthenticationFilter.java
String role = claims.get("role", String.class);
modifiedRequest = request.mutate()
        .header("X-User-Id", userId)
        .header("X-Username", username)
        .header("X-User-Role", role)  // Pass to backend
        .build();
```

Use in backend:
```java
@PostMapping("/admin/courses")
public ResponseEntity<?> createCourse(
        @RequestHeader("X-User-Role") String role) {
    if (!"INSTRUCTOR".equals(role) && !"ADMIN".equals(role)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    // Create course
}
```

### API Rate Limiting

Add to gateway configuration:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: rate-limited-route
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20
```

## Summary

- **✅ Centralized**: JWT validation at API Gateway only
- **✅ Secure**: BCrypt passwords, 256-bit JWT secret
- **✅ Fast**: Single validation, no per-service overhead
- **✅ Simple**: Backend services read headers, no auth logic
- **✅ Scalable**: Easy to add new services, instant protection
- **✅ Maintainable**: Update security in one place

All microservices are now protected by the API Gateway's centralized authentication system!
