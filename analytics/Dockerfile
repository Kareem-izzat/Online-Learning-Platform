FROM maven:3.9.3-eclipse-temurin-17 as build
WORKDIR /workspace
COPY pom.xml ./
COPY src ./src
RUN mvn -DskipTests package -DskipTests

FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=build /workspace/target/analytics-1.0.0-SNAPSHOT.jar ./analytics.jar
EXPOSE 8100
ENTRYPOINT ["java","-jar","/app/analytics.jar"]
