# Multi-stage Dockerfile to build and run the application

# ====== Build stage ======
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /workspace/app

# Copy Gradle wrapper and settings for better layer caching
COPY gradlew ./
COPY gradle ./gradle
COPY settings.gradle.kts build.gradle.kts ./

# Copy module build files first to leverage caching of dependency download
COPY application/build.gradle.kts application/build.gradle.kts
COPY bootstrap/build.gradle.kts bootstrap/build.gradle.kts
COPY domain/build.gradle.kts domain/build.gradle.kts
COPY infrastructure/driven-adapters/jpa-repository/build.gradle.kts infrastructure/driven-adapters/jpa-repository/build.gradle.kts
COPY infrastructure/driven-adapters/kafka-producer/build.gradle.kts infrastructure/driven-adapters/kafka-producer/build.gradle.kts
COPY infrastructure/entry-points/rest-controller/build.gradle.kts infrastructure/entry-points/rest-controller/build.gradle.kts
COPY infrastructure/entry-points/kafka-consumer/build.gradle.kts infrastructure/entry-points/kafka-consumer/build.gradle.kts

# Pre-download dependencies (will do a no-op build without sources)
RUN chmod +x gradlew && ./gradlew --no-daemon help || true

# Now copy the full source code
COPY . .

# Build the Spring Boot executable jar for the bootstrap module (skip tests for faster image build)
RUN ./gradlew --no-daemon clean :bootstrap:bootJar -x test

# ====== Runtime stage ======
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Copy the built application
COPY --from=builder /workspace/app/bootstrap/build/libs/devsu-movements.jar /app/app.jar

# Expose application port (configured default in application.yml)
EXPOSE 8181

# Optional JVM args (e.g., -Xms256m -Xmx512m)
ENV JAVA_OPTS=""

# Spring profile can be controlled via env var
ENV SPRING_PROFILES_ACTIVE="local"

# Start the application
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
