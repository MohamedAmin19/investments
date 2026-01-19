# Multi-stage build for Spring Boot application
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage - Using Debian-based image instead of Alpine to avoid native library issues
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Install curl for healthcheck and create non-root user (must be done as root)
RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/* && \
    groupadd -r spring && \
    useradd -r -g spring spring

# Switch to non-root user
USER spring:spring

# Copy the JAR from build stage
COPY --from=build /app/target/Investment-0.0.1-SNAPSHOT.jar app.jar

# Expose port (Render will set PORT env variable)
EXPOSE 8080

# Health check (using curl which is available in Debian images)
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:${PORT:-8080}/api/ping || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

