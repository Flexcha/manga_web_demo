# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy only backend (Dockerfile is at root, pom.xml is in backend/)
COPY backend/pom.xml ./pom.xml
COPY backend/src ./src

RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# The artifact from pom.xml: groupId=com.cuutruyen, artifactId=backend, version=0.0.1-SNAPSHOT → backend-0.0.1-SNAPSHOT.jar
COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# Render provides PORT env var; Spring Boot reads server.port from it
ENTRYPOINT exec java -Dserver.port=${PORT:-8080} -jar app.jar
