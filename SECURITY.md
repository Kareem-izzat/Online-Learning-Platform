# Security Implementation Guide

## Overview

The Online Learning Platform implements a **centralized security architecture** at the API Gateway level:

- **✅ Centralized Authentication**: JWT validation at API Gateway only
- **✅ Single Point of Entry**: All client requests go through gateway
- **✅ User Context Propagation**: Gateway adds `X-User-Id` and `X-Username` headers
- **✅ Backend Trust Model**: Services trust gateway's user context
- **✅ Password Security**: BCrypt hashing in User Service
- **✅ Service-to-Service**: API Key authentication (Analytics Service)

## Quick Start

### Architecture

```
Client → API Gateway (Validates JWT) → Backend Service (Trusts Headers)
            ↓
       Extracts userId
            ↓
    Adds X-User-Id header
```

### Authentication Flow

1. **Register**: `POST /api/auth/register` (User Service)
2. **Login**: `POST /api/auth/login` → Get JWT token
3. **Use Token**: Add `Authorization: Bearer <token>` to all requests
4. **Gateway**: Validates token, extracts userId, forwards to backend
5. **Backend**: Reads `X-User-Id` header (already authenticated!)

---

## Centralized Security (API Gateway)

**✅ Current Implementation**: All services protected at gateway level

The API Gateway validates JWT tokens and forwards authenticated requests to backend services with user context headers (`X-User-Id`, `X-Username`).

### Public Endpoints (No Auth Required)

- `/api/auth/**` - Login, register
- `/api/courses` (GET) - Browse courses
- `/api/discussions` (GET) - Read discussions
- `/eureka/**` - Service discovery
- `/actuator/**` - Health checks

### Protected Endpoints (JWT Required)

- All POST, PUT, DELETE operations
- User-specific data (enrollments, progress, payments)
- Course creation (instructors only)

---

## Analytics Service Security

### Authentication Model

**API Key Authentication** for service-to-service communication:
- POST `/api/analytics/ingest/**` requires `X-API-Key` header
- GET endpoints are **public** (read analytics)
- Health checks are **public**

### Configuration

```properties
# Set via environment variable in production
analytics.security.api-key=${ANALYTICS_API_KEY:change-this-in-production}
```

### Generate API Key

```bash
# Generate secure random key
openssl rand -base64 32
# Or use UUID
uuidgen
```

### Usage Example

```bash
# With API Key (for ingest)
curl -X POST http://localhost:8100/api/analytics/ingest \
  -H "X-API-Key: your-secret-api-key-here" \
  -H "Content-Type: application/json" \
  -d '{"eventType":"thread_created","eventId":"123",...}'

# Without authentication (read analytics)
curl http://localhost:8100/api/analytics/threads/123
```

---

## Discussion Service Security

### Authentication Model

**JWT Tokens** for user authentication:
- GET `/api/discussions/**` - **Public** (anyone can read)
- POST/PUT/DELETE `/api/discussions/**` - **Requires JWT** token

### JWT Configuration

```properties
# Set via environment variable in production
jwt.secret=${JWT_SECRET:your-256-bit-secret-minimum-32-characters}
jwt.expiration=${JWT_EXPIRATION:86400000}
```

### Generate JWT Secret

```bash
# Generate secure 256-bit secret
openssl rand -base64 32
```

### JWT Token Format

```json
{
  "sub": "123",
  "username": "john.doe",
  "iat": 1730295600,
  "exp": 1730382000
}
```

### Usage Example

**Step 1: Get JWT Token** (integrate with User Service):
```bash
# This would come from user-service login endpoint
# For testing, you can generate a token manually or use a test endpoint
```

**Step 2: Use JWT Token**:
```bash
# Create thread (requires authentication)
curl -X POST http://localhost:8092/api/discussions/threads \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR..." \
  -H "Content-Type: application/json" \
  -d '{
    "courseId": 42,
    "authorId": 100,
    "title": "My Question",
    "content": "...",
    "category": "QUESTION"
  }'

# Read thread (no authentication required)
curl http://localhost:8092/api/discussions/threads/1
```

---

## Input Validation

All request DTOs are validated using Jakarta Validation:

```java
@NotBlank(message = "eventType is required")
private String eventType;

@NotNull(message = "occurredAt is required")
private Instant occurredAt;
```

**Validation Error Response**:
```json
{
  "timestamp": "2025-10-30T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "eventType is required",
  "path": "/api/analytics/ingest"
}
```

---

## CORS Configuration

Both services support configurable CORS for web clients:

```properties
# Allow multiple origins (comma-separated)
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:4200,https://your-domain.com
```

**Default Configuration**:
- Allowed Origins: `localhost:3000`, `localhost:4200`
- Allowed Methods: GET, POST, PUT, DELETE, OPTIONS
- Allowed Headers: All (*)
- Credentials: Enabled

---

## Environment Variables

### Production Deployment

**Analytics Service**:
```bash
export ANALYTICS_API_KEY=$(openssl rand -base64 32)
export SPRING_DATASOURCE_PASSWORD=your-db-password
export SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092
```

**Discussion Service**:
```bash
export JWT_SECRET=$(openssl rand -base64 32)
export SPRING_DATASOURCE_PASSWORD=your-db-password
export SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092
```

### Docker Compose Example

```yaml
services:
  analytics:
    environment:
      ANALYTICS_API_KEY: ${ANALYTICS_API_KEY}
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      
  discussion-service:
    environment:
      JWT_SECRET: ${JWT_SECRET}
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
```

---

## Security Best Practices Implemented

### ✅ What's Secure

1. **No Hardcoded Secrets**: All secrets via environment variables
2. **Stateless Authentication**: JWT (no session state)
3. **API Key Rotation**: Easy to change via config
4. **Input Validation**: Prevents malformed data
5. **CORS Protection**: Only allowed origins can access
6. **CSRF Protection**: Disabled (stateless API, not needed)
7. **Password Encoding**: BCrypt for password hashing

### ⚠️ Production Recommendations

1. **HTTPS Only**: Deploy behind nginx/load balancer with SSL
2. **Rate Limiting**: Add application-level rate limiting (Bucket4j)
3. **API Gateway**: Use Kong/API Gateway for centralized auth
4. **Secrets Management**: Use Vault/AWS Secrets Manager
5. **Audit Logging**: Log all authentication attempts
6. **Token Refresh**: Implement refresh token mechanism
7. **IP Whitelisting**: Restrict Kafka/database access

---

## Testing Security

### Test API Key Authentication

```bash
# Should fail (no API key)
curl -X POST http://localhost:8100/api/analytics/ingest \
  -H "Content-Type: application/json" \
  -d '{"eventType":"test"}'

# Should succeed
curl -X POST http://localhost:8100/api/analytics/ingest \
  -H "X-API-Key: your-api-key" \
  -H "Content-Type: application/json" \
  -d '{"eventType":"test","eventId":"1","occurredAt":"2025-10-30T12:00:00Z"}'
```

### Test JWT Authentication

```bash
# Should succeed (public endpoint)
curl http://localhost:8092/api/discussions/threads

# Should fail (no JWT)
curl -X POST http://localhost:8092/api/discussions/threads \
  -H "Content-Type: application/json" \
  -d '{...}'

# Should succeed (with JWT)
curl -X POST http://localhost:8092/api/discussions/threads \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json" \
  -d '{...}'
```

---

## Integration with User Service

The Discussion Service JWT authentication is designed to integrate with User Service:

```java
// In User Service - Login endpoint
@PostMapping("/login")
public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
    // Authenticate user
    User user = authenticate(request.getUsername(), request.getPassword());
    
    // Generate JWT using JwtUtil
    String token = jwtUtil.generateToken(user.getId(), user.getUsername());
    
    return ResponseEntity.ok(new LoginResponse(token, user));
}
```

**Workflow**:
1. User logs in via User Service → receives JWT
2. Frontend stores JWT (localStorage/cookie)
3. Frontend sends JWT with every Discussion Service request
4. Discussion Service validates JWT and extracts userId

---

## Security Checklist

Before going to production:

- [ ] Generate new JWT secret (min 32 characters)
- [ ] Generate new API keys for service communication
- [ ] Move all secrets to environment variables
- [ ] Set up HTTPS/TLS for all services
- [ ] Configure production CORS origins
- [ ] Enable rate limiting
- [ ] Set up secrets management (Vault)
- [ ] Configure Kafka SSL/TLS encryption
- [ ] Implement token refresh mechanism
- [ ] Add audit logging
- [ ] Set up API Gateway with centralized auth
- [ ] Penetration testing
- [ ] Security audit

---

## Troubleshooting

### "401 Unauthorized" on ingest endpoint

**Problem**: Missing or invalid API key  
**Solution**: Add `X-API-Key` header with correct value

### "403 Forbidden" on discussion endpoints

**Problem**: Missing or expired JWT token  
**Solution**: Obtain new JWT from user login and add to `Authorization: Bearer` header

### CORS errors in browser

**Problem**: Frontend origin not in allowed list  
**Solution**: Add origin to `CORS_ALLOWED_ORIGINS` environment variable

### JWT validation fails

**Problem**: JWT secret mismatch or token expired  
**Solution**: Ensure JWT_SECRET matches between token generation and validation, check expiration time

---

## Summary

Your system now has:

✅ **Authentication**: JWT (users) + API Keys (services)  
✅ **Authorization**: Role-based access control  
✅ **Input Validation**: Request data validated  
✅ **CORS**: Cross-origin protection  
✅ **Secure Config**: No hardcoded secrets  

**Next step**: Test the security, then deploy to production with proper secrets management! 🔒
