# Multi-stage Dockerfile for Discovery Service (Eureka)
# Builds the JAR and produces a small runtime image

FROM maven:3.8.8-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre-alpine
ARG JAR_FILE=/workspace/target/*.jar
COPY --from=build ${JAR_FILE} /app/discovery.jar
EXPOSE 8761
ENTRYPOINT ["sh","-c","java -jar /app/discovery.jar"]
