# Discovery Service

Eureka Discovery Service for the Online Learning Platform microservices architecture.

## Overview
This service acts as a service registry where all microservices register themselves. It enables:
- Dynamic service discovery
- Load balancing
- Health monitoring
- Failover support

## Configuration
- **Port:** 8761
- **Dashboard URL:** http://localhost:8761
- **Eureka Server:** Does not register with itself

## Features
- Service registration and discovery
- Real-time health monitoring
- Web dashboard for viewing registered services
- Auto-eviction of failed instances

## Registered Services
Once running, the following services will register:
- User Service (port 8081)
- Course Service (port 8082) - *Coming soon*
- Enrollment Service (port 8083) - *Coming soon*
- And more...

## Running the Service

### Using Maven
```bash
cd discovery-service
mvn spring-boot:run
```

### Using Spring Boot Dashboard
Click the play button next to `discovery-service` in VS Code's Spring Boot Dashboard.

## Accessing the Dashboard
Once started, open your browser to:
```
http://localhost:8761
```

You'll see all registered microservices with their status, instances, and health information.
