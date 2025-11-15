# Multi-stage Dockerfile for API Gateway
# Stage 1: Build the application
FROM maven:3.8.8-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests clean package

# Stage 2: Runtime image with non-root user
FROM eclipse-temurin:17-jre-alpine
LABEL maintainer="learning-platform"
LABEL description="API Gateway with JWT Authentication"

# Install curl for healthcheck
RUN apk add --no-cache curl

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring

# Set working directory
WORKDIR /app

# Copy JAR from build stage
COPY --from=build --chown=spring:spring /workspace/target/*.jar /app/api-gateway.jar

# Switch to non-root user
USER spring

# Health check endpoint
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

EXPOSE 8080
ENTRYPOINT ["sh","-c","java ${JAVA_OPTS} -jar /app/api-gateway.jar"]
