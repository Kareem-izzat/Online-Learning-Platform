# Multi-stage Dockerfile for API Gateway
# Builds the jar with Maven and runs with a slim JRE

FROM maven:3.8.8-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre-alpine
ARG JAR_FILE=/workspace/target/*.jar
COPY --from=build ${JAR_FILE} /app/api-gateway.jar
EXPOSE 8080
# Use environment variable to set JVM options if needed
ENTRYPOINT ["sh","-c","java ${JAVA_OPTS:-} -jar /app/api-gateway.jar"]
