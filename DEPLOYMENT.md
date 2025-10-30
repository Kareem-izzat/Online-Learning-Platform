# Production Deployment Guide

## Quick Start

This guide covers deploying the Online Learning Platform with Analytics and Discussion services in a production environment.

## Prerequisites

- Docker & Docker Compose
- Java 17+
- PostgreSQL 15
- Apache Kafka 3.7+
- Nginx (reverse proxy)

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                      Nginx (SSL/TLS)                        │
│                  (Port 443 - HTTPS)                         │
└────────────┬──────────────────────────────┬─────────────────┘
             │                              │
    ┌────────▼────────┐          ┌─────────▼──────────┐
    │ Discussion Svc  │          │  Analytics Service │
    │   (Port 8092)   │          │    (Port 8100)     │
    └────────┬────────┘          └─────────┬──────────┘
             │                              │
    ┌────────▼────────────────────────────┬─▼──────────┐
    │         Apache Kafka (Port 9092)               │
    └────────┬────────────────────────────────────────┘
             │
    ┌────────▼────────┐          ┌──────────────────┐
    │  PostgreSQL     │          │  PostgreSQL      │
    │  (Discussion)   │          │  (Analytics)     │
    └─────────────────┘          └──────────────────┘
```

---

## Step 1: Generate Secrets

```bash
# Generate JWT secret (256-bit minimum)
export JWT_SECRET=$(openssl rand -base64 32)

# Generate API key for service communication
export ANALYTICS_API_KEY=$(openssl rand -base64 32)

# Generate database passwords
export DB_PASSWORD_DISCUSSION=$(openssl rand -base64 32)
export DB_PASSWORD_ANALYTICS=$(openssl rand -base64 32)

# Save to .env file (DO NOT commit to git)
cat > .env <<EOF
JWT_SECRET=${JWT_SECRET}
ANALYTICS_API_KEY=${ANALYTICS_API_KEY}
DB_PASSWORD_DISCUSSION=${DB_PASSWORD_DISCUSSION}
DB_PASSWORD_ANALYTICS=${DB_PASSWORD_ANALYTICS}
CORS_ALLOWED_ORIGINS=https://your-domain.com
EOF
```

---

## Step 2: Configure Application Properties

### Analytics Service

Create `analytics/src/main/resources/application-prod.properties`:

```properties
# Server
server.port=8100

# Database
spring.datasource.url=jdbc:postgresql://postgres-analytics:5432/analytics_db
spring.datasource.username=analytics
spring.datasource.password=${DB_PASSWORD_ANALYTICS}
spring.jpa.hibernate.ddl-auto=validate

# Kafka
analytics.kafka.enabled=true
spring.kafka.bootstrap-servers=kafka:9092

# Security
analytics.security.api-key=${ANALYTICS_API_KEY}
analytics.security.cors.allowed-origins=${CORS_ALLOWED_ORIGINS}

# Logging
logging.level.root=INFO
logging.level.com.learnit.analytics=INFO
```

### Discussion Service

Create `discussion-service/src/main/resources/application-prod.properties`:

```properties
# Server
server.port=8092

# Database
spring.datasource.url=jdbc:postgresql://postgres-discussion:5432/discussion_service_db
spring.datasource.username=discussion
spring.datasource.password=${DB_PASSWORD_DISCUSSION}
spring.jpa.hibernate.ddl-auto=validate

# Kafka
discussion.kafka.enabled=true
spring.kafka.bootstrap-servers=kafka:9092

# Security
jwt.secret=${JWT_SECRET}
jwt.expiration=86400000
discussion.security.cors.allowed-origins=${CORS_ALLOWED_ORIGINS}

# Logging
logging.level.root=INFO
logging.level.com.learnit.discussion=INFO
```

---

## Step 3: Build Applications

```bash
# Build Analytics Service
cd analytics
mvn clean package -DskipTests

# Build Discussion Service
cd ../discussion-service
mvn clean package -DskipTests
```

---

## Step 4: Docker Compose Production

Create `docker-compose.prod.yml`:

```yaml
version: '3.8'

services:
  postgres-discussion:
    image: postgres:15
    environment:
      POSTGRES_DB: discussion_service_db
      POSTGRES_USER: discussion
      POSTGRES_PASSWORD: ${DB_PASSWORD_DISCUSSION}
    volumes:
      - discussion-data:/var/lib/postgresql/data
    networks:
      - backend
    restart: always

  postgres-analytics:
    image: postgres:15
    environment:
      POSTGRES_DB: analytics_db
      POSTGRES_USER: analytics
      POSTGRES_PASSWORD: ${DB_PASSWORD_ANALYTICS}
    volumes:
      - analytics-data:/var/lib/postgresql/data
    networks:
      - backend
    restart: always

  kafka:
    image: apache/kafka:3.7.0
    environment:
      KAFKA_NODE_ID: 1
      KAFKA_PROCESS_ROLES: broker,controller
      KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:9093
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
      KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@kafka:9093
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_NUM_PARTITIONS: 3
    volumes:
      - kafka-data:/var/lib/kafka/data
    networks:
      - backend
    restart: always

  discussion-service:
    build: ./discussion-service
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres-discussion:5432/discussion_service_db
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD_DISCUSSION}
      SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
      JWT_SECRET: ${JWT_SECRET}
      CORS_ALLOWED_ORIGINS: ${CORS_ALLOWED_ORIGINS}
    depends_on:
      - postgres-discussion
      - kafka
    networks:
      - backend
    restart: always

  analytics-service:
    build: ./analytics
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres-analytics:5432/analytics_db
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD_ANALYTICS}
      SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
      ANALYTICS_API_KEY: ${ANALYTICS_API_KEY}
      CORS_ALLOWED_ORIGINS: ${CORS_ALLOWED_ORIGINS}
    depends_on:
      - postgres-analytics
      - kafka
    networks:
      - backend
    restart: always

  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf:ro
      - ./ssl:/etc/nginx/ssl:ro
    depends_on:
      - discussion-service
      - analytics-service
    networks:
      - backend
    restart: always

volumes:
  discussion-data:
  analytics-data:
  kafka-data:

networks:
  backend:
    driver: bridge
```

---

## Step 5: Nginx Configuration

Create `nginx.conf`:

```nginx
events {
    worker_connections 1024;
}

http {
    upstream discussion_service {
        server discussion-service:8092;
    }

    upstream analytics_service {
        server analytics-service:8100;
    }

    # Redirect HTTP to HTTPS
    server {
        listen 80;
        server_name your-domain.com;
        return 301 https://$server_name$request_uri;
    }

    # HTTPS Server
    server {
        listen 443 ssl http2;
        server_name your-domain.com;

        ssl_certificate /etc/nginx/ssl/fullchain.pem;
        ssl_certificate_key /etc/nginx/ssl/privkey.pem;
        ssl_protocols TLSv1.2 TLSv1.3;
        ssl_ciphers HIGH:!aNULL:!MD5;

        # Discussion Service
        location /api/discussions/ {
            proxy_pass http://discussion_service;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }

        # Analytics Service
        location /api/analytics/ {
            proxy_pass http://analytics_service;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }

        # Health checks
        location /health {
            access_log off;
            return 200 "healthy\n";
            add_header Content-Type text/plain;
        }
    }
}
```

---

## Step 6: SSL Certificate

```bash
# Using Let's Encrypt (free)
sudo apt install certbot
sudo certbot certonly --standalone -d your-domain.com

# Copy certificates
mkdir -p ssl
sudo cp /etc/letsencrypt/live/your-domain.com/fullchain.pem ssl/
sudo cp /etc/letsencrypt/live/your-domain.com/privkey.pem ssl/
```

---

## Step 7: Deploy

```bash
# Load environment variables
source .env

# Start all services
docker-compose -f docker-compose.prod.yml up -d

# Check status
docker-compose -f docker-compose.prod.yml ps

# View logs
docker-compose -f docker-compose.prod.yml logs -f
```

---

## Step 8: Database Migrations

```bash
# Create Flyway migrations (recommended for production)
# Example: discussion-service/src/main/resources/db/migration/V1__Initial_Schema.sql

# Run migrations
docker-compose -f docker-compose.prod.yml exec discussion-service \
  java -jar app.jar --spring.flyway.enabled=true
```

---

## Monitoring & Health Checks

### Health Endpoints

```bash
# Discussion Service
curl https://your-domain.com/api/discussions/actuator/health

# Analytics Service
curl https://your-domain.com/api/analytics/actuator/health
```

### Kafka Monitoring

```bash
# Check consumer group lag
docker exec kafka kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --describe --group analytics-service
```

### Database Connections

```bash
# Check active connections
docker exec postgres-discussion psql -U discussion -d discussion_service_db \
  -c "SELECT COUNT(*) FROM pg_stat_activity;"
```

---

## Backup Strategy

### Database Backups

```bash
# Automated daily backups
cat > backup.sh <<'EOF'
#!/bin/bash
DATE=$(date +%Y%m%d)
docker exec postgres-discussion pg_dump -U discussion discussion_service_db | gzip > backup-discussion-$DATE.sql.gz
docker exec postgres-analytics pg_dump -U analytics analytics_db | gzip > backup-analytics-$DATE.sql.gz
# Upload to S3 or backup storage
EOF

chmod +x backup.sh

# Add to crontab (daily at 2 AM)
0 2 * * * /path/to/backup.sh
```

---

## Scaling

### Horizontal Scaling

```yaml
# Scale analytics service to 3 instances
docker-compose -f docker-compose.prod.yml up -d --scale analytics-service=3

# Kafka will automatically distribute load across consumers
```

### Kafka Partitions

```bash
# Increase partitions for better parallelism
docker exec kafka kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --alter --topic discussion.events \
  --partitions 6
```

---

## Troubleshooting

### Service won't start

```bash
# Check logs
docker-compose -f docker-compose.prod.yml logs service-name

# Common issues:
# - Database connection failed → check passwords
# - Kafka not ready → increase startup wait time
# - Port already in use → change port mapping
```

### Events not flowing

```bash
# Check outbox table
docker exec postgres-discussion psql -U discussion -d discussion_service_db \
  -c "SELECT COUNT(*) FROM outbox_events WHERE processed = false;"

# Check Kafka topic
docker exec kafka kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic discussion.events \
  --from-beginning --max-messages 10
```

---

## Security Checklist

Before production:

- [ ] Change all default passwords
- [ ] Enable HTTPS/TLS everywhere
- [ ] Rotate secrets regularly
- [ ] Set up firewall rules
- [ ] Enable audit logging
- [ ] Configure rate limiting
- [ ] Set up monitoring/alerts
- [ ] Perform security audit
- [ ] Enable database encryption at rest
- [ ] Configure backup retention

---

## Maintenance

### Update Services

```bash
# Pull latest code
git pull

# Rebuild and restart
docker-compose -f docker-compose.prod.yml build
docker-compose -f docker-compose.prod.yml up -d
```

### Cleanup Old Data

```sql
-- Delete old processed outbox events (older than 30 days)
DELETE FROM outbox_events 
WHERE processed = true 
AND processed_at < NOW() - INTERVAL '30 days';

-- Archive old analytics data
-- Move to cold storage or time-series database
```

---

## Cost Optimization

- **Kafka**: Single broker for small loads, cluster for production
- **Database**: Use connection pooling, read replicas for scaling
- **Analytics**: Consider ClickHouse/TimescaleDB for OLAP
- **Monitoring**: Use open-source stack (Prometheus + Grafana)

---

## Next Steps

1. Set up CI/CD pipeline (GitHub Actions)
2. Configure monitoring (Prometheus + Grafana)
3. Implement log aggregation (ELK stack)
4. Add distributed tracing (Jaeger/Zipkin)
5. Performance testing (JMeter/Gatling)
6. Disaster recovery plan
7. Documentation for operations team

---

Your system is now production-ready! 🚀
