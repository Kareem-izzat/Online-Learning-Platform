# Docker Setup Guide

Complete guide for running the Online Learning Platform with Docker and Docker Compose.

## 📋 Table of Contents

- [Overview](#overview)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Architecture](#architecture)
- [Configuration](#configuration)
- [Service Management](#service-management)
- [Troubleshooting](#troubleshooting)
- [Production Deployment](#production-deployment)

## Overview

The platform provides Docker support with:
- **Multi-stage Dockerfiles**: Optimized build and runtime images
- **Docker Compose**: Orchestrates core services with proper dependencies
- **Healthchecks**: Ensures services start in correct order
- **Non-root users**: Enhanced security
- **Environment-based config**: Easy customization via `.env` file

## Prerequisites

- **Docker**: 20.10+ ([Install Docker](https://docs.docker.com/get-docker/))
- **Docker Compose**: 2.0+ (included with Docker Desktop)
- **Git**: For cloning the repository
- **8GB RAM**: Minimum for running core services
- **10GB Disk**: For images and volumes

Check installation:
```powershell
docker --version
docker compose version
```

## Quick Start

### 1. Clone and Configure

```powershell
# Clone repository
git clone https://github.com/Kareem-izzat/Online-Learning-Platform.git
cd Online-Learning-Platform

# Copy environment template
copy .env.example .env

# Edit .env with your settings
notepad .env
```

### 2. Start Core Services

```powershell
# Build and start (first time takes 5-10 minutes)
docker compose up --build -d

# Check status
docker compose ps

# View logs
docker compose logs -f
```

### 3. Verify Services

```powershell
# Eureka Dashboard
start http://localhost:8761

# Test API Gateway health
curl http://localhost:8080/actuator/health

# Register a user
curl -X POST http://localhost:8080/api/auth/register `
  -H "Content-Type: application/json" `
  -d '{\"email\":\"test@example.com\",\"password\":\"test123\",\"firstName\":\"Test\",\"lastName\":\"User\",\"role\":\"STUDENT\"}'
```

## Architecture

### Service Stack

```
┌─────────────────────────────────────────────────┐
│           API Gateway (Port 8080)               │
│         JWT Authentication & Routing            │
└────────────────┬────────────────────────────────┘
                 │
    ┌────────────┼────────────┐
    │            │            │
┌───▼───────┐ ┌──▼────────┐ ┌▼──────────────────┐
│ Discovery │ │   User    │ │    PostgreSQL     │
│  Service  │ │  Service  │ │    (Port 5432)    │
│ (8761)    │ │  (8081)   │ │                   │
└───────────┘ └───────────┘ └───────────────────┘
```

### Container Details

| Service | Base Image | Size | Purpose |
|---------|-----------|------|---------|
| postgres | postgres:15 | ~400MB | Primary database |
| discovery-service | eclipse-temurin:17-jre-alpine | ~180MB | Service registry |
| user-service | eclipse-temurin:17-jre-alpine | ~200MB | Authentication |
| api-gateway | eclipse-temurin:17-jre-alpine | ~190MB | Gateway & routing |

### Network

All services run on the `learning-network` bridge network:
- Internal DNS resolution (services resolve by name)
- Isolated from host network by default
- Only mapped ports are exposed to host

## Configuration

### Environment Variables

The `.env` file controls all configuration. See `.env.example` for all options.

**Essential Variables**:

```properties
# Database
POSTGRES_PASSWORD=your-secure-password

# JWT (CRITICAL - must be 32+ characters)
JWT_SECRET=generate-with-openssl-rand-base64-32

# Ports (customize if conflicts exist)
DISCOVERY_PORT=8761
API_GATEWAY_PORT=8080
USER_SERVICE_PORT=8081
```

### Generate Secure JWT Secret

```powershell
# Windows (requires OpenSSL)
openssl rand -base64 32

# Or use PowerShell
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Minimum 0 -Maximum 256 }))
```

### JVM Tuning

Set memory limits and GC options:

```properties
# .env file
JAVA_OPTS=-Xmx512m -Xms256m -XX:+UseG1GC -XX:MaxGCPauseMillis=200
```

### Database Persistence

Data is persisted in Docker volume `postgres-data`:

```powershell
# View volumes
docker volume ls

# Inspect volume
docker volume inspect online-learning-platform_postgres-data

# Backup volume
docker run --rm -v online-learning-platform_postgres-data:/data -v ${PWD}:/backup alpine tar czf /backup/db-backup.tar.gz -C /data .

# Restore volume
docker run --rm -v online-learning-platform_postgres-data:/data -v ${PWD}:/backup alpine tar xzf /backup/db-backup.tar.gz -C /data
```

## Service Management

### Start/Stop

```powershell
# Start all services
docker compose up -d

# Start specific service
docker compose up -d user-service

# Stop all services (keeps volumes)
docker compose down

# Stop and remove volumes (clean slate)
docker compose down -v
```

### Logs

```powershell
# All services
docker compose logs -f

# Specific service
docker compose logs -f api-gateway

# Last 100 lines
docker compose logs --tail=100 user-service

# Logs since timestamp
docker compose logs --since 2024-01-01T10:00:00 discovery-service
```

### Rebuild

```powershell
# Rebuild all services
docker compose up --build -d

# Rebuild specific service
docker compose build user-service
docker compose up -d user-service

# Force rebuild (no cache)
docker compose build --no-cache api-gateway
```

### Scale Services

```powershell
# Scale user service to 3 instances
docker compose up -d --scale user-service=3

# Note: Requires load balancer configuration
```

### Execute Commands

```powershell
# Shell into container
docker compose exec user-service sh

# Run command
docker compose exec postgres psql -U postgres -d user_service_db

# Check Java version
docker compose exec api-gateway java -version

# View environment variables
docker compose exec user-service env
```

### Health Monitoring

```powershell
# Check health status
docker compose ps

# Detailed service info
docker inspect $(docker compose ps -q user-service)

# Follow health checks
watch docker compose ps
```

## Troubleshooting

### Services Won't Start

**Symptom**: Services remain in "starting" state

**Solutions**:
```powershell
# Check logs for errors
docker compose logs

# Verify healthchecks
docker compose ps

# Ensure ports are available
netstat -ano | findstr "8080 8081 8761 5432"

# Restart problematic service
docker compose restart user-service
```

### Database Connection Errors

**Symptom**: `Connection refused` or `Unknown host`

**Solutions**:
```powershell
# Verify Postgres is running
docker compose exec postgres pg_isready -U postgres

# Check network connectivity
docker compose exec user-service ping postgres

# Verify environment variables
docker compose exec user-service env | findstr DATASOURCE

# Restart with clean database
docker compose down -v
docker compose up -d
```

### Memory Issues

**Symptom**: Services crash with `OutOfMemoryError`

**Solutions**:
```powershell
# Increase JVM memory in .env
JAVA_OPTS=-Xmx1024m -Xms512m

# Check container memory usage
docker stats

# Increase Docker Desktop memory
# Settings > Resources > Memory > 8GB+
```

### Build Failures

**Symptom**: Maven build fails in Docker

**Solutions**:
```powershell
# Clear Maven cache and rebuild
docker compose build --no-cache

# Check parent pom exists
ls pom.xml

# Build locally first to verify
mvn clean install -DskipTests
```

### Port Conflicts

**Symptom**: `port is already allocated`

**Solutions**:
```powershell
# Find process using port
netstat -ano | findstr "8080"

# Kill process
taskkill /F /PID <process-id>

# Or change port in .env
API_GATEWAY_PORT=8090
```

### JWT Token Issues

**Symptom**: `Invalid or expired JWT token`

**Solutions**:
1. Ensure JWT_SECRET matches in both api-gateway and user-service
2. Check `.env` file is loaded: `docker compose config | findstr JWT_SECRET`
3. Verify token not expired (default 24 hours)
4. Generate new token by logging in again

### Slow Performance

**Solutions**:
```powershell
# Enable BuildKit for faster builds
$env:DOCKER_BUILDKIT=1
docker compose build

# Use layer caching
# (Already enabled in Dockerfiles)

# Prune unused resources
docker system prune -a --volumes

# Increase Docker resources
# Settings > Resources > CPU/Memory
```

## Production Deployment

### Security Hardening

1. **Secrets Management**:
```yaml
# Use Docker secrets instead of .env
secrets:
  jwt_secret:
    external: true
  db_password:
    external: true

services:
  user-service:
    secrets:
      - jwt_secret
      - db_password
```

2. **Network Isolation**:
```yaml
networks:
  frontend:
    driver: overlay
  backend:
    driver: overlay
    internal: true  # No external access
```

3. **Read-only Filesystem**:
```yaml
services:
  api-gateway:
    read_only: true
    tmpfs:
      - /tmp
```

4. **Resource Limits**:
```yaml
services:
  user-service:
    deploy:
      resources:
        limits:
          cpus: '1.0'
          memory: 512M
        reservations:
          cpus: '0.5'
          memory: 256M
```

### Kubernetes Migration

Convert to Kubernetes with Kompose:

```powershell
# Install Kompose
choco install kubernetes-kompose

# Convert docker-compose.yml
kompose convert

# Deploy to Kubernetes
kubectl apply -f .
```

### CI/CD Pipeline

Example GitHub Actions workflow:

```yaml
name: Docker Build and Push

on:
  push:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Login to Docker Hub
        uses: docker/login-action@v2
        with:
          username: ${{ secrets.DOCKER_USERNAME }}
          password: ${{ secrets.DOCKER_PASSWORD }}
      
      - name: Build and Push
        run: |
          docker compose build
          docker compose push
```

### Monitoring

Add Prometheus and Grafana:

```yaml
# docker-compose.monitoring.yml
services:
  prometheus:
    image: prom/prometheus
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
    ports:
      - "9090:9090"
  
  grafana:
    image: grafana/grafana
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
```

### High Availability

Deploy with Docker Swarm:

```powershell
# Initialize swarm
docker swarm init

# Deploy stack
docker stack deploy -c docker-compose.yml learning-platform

# Scale services
docker service scale learning-platform_user-service=3
```

## Additional Resources

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Reference](https://docs.docker.com/compose/compose-file/)
- [Spring Boot Docker Guide](https://spring.io/guides/topicals/spring-boot-docker/)
- [Project DEPLOYMENT.md](DEPLOYMENT.md)

## Support

For issues and questions:
- Check [Troubleshooting](#troubleshooting) section
- Review service logs: `docker compose logs`
- Open GitHub issue with logs and configuration

---

**Happy Dockering! 🐳**
