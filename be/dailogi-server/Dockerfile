# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy pom.xml and .mvn directory to leverage Docker cache for dependencies
COPY .mvn/ .mvn/
COPY pom.xml .

# Download dependencies
# Using dependency:go-offline first to leverage Docker cache better
RUN mvn dependency:go-offline

# Copy the source code
COPY src ./src

# Package the application
RUN mvn package -DskipTests

# Stage 2: Create the runtime image
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create a non-root user and group
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy the JAR file from the builder stage
COPY --from=builder /app/target/dailogi-server-*.jar app.jar

# Set file ownership to the non-root user
RUN chown appuser:appgroup app.jar

# Switch to the non-root user
USER appuser

# Expose the application port (default 8080, can be overridden by SERVER_PORT env var)
EXPOSE 8080
EXPOSE 8443

# Default port, can be overridden by environment variable at runtime
ENV SERVER_PORT=8080

# Healthcheck
# Spring Boot Actuator health endpoint
HEALTHCHECK --interval=30s --timeout=5s --start-period=15s --retries=3 \
  CMD wget -q --spider http://localhost:${SERVER_PORT}/actuator/health || exit 1

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"] 