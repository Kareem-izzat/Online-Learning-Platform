# Multi-stage Dockerfile for User Service

FROM maven:3.8.8-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre-alpine
ARG JAR_FILE=/workspace/target/*.jar
COPY --from=build ${JAR_FILE} /app/user-service.jar
EXPOSE 8081
ENTRYPOINT ["sh","-c","java ${JAVA_OPTS:-} -jar /app/user-service.jar"]
