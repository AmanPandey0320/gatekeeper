# Build stage
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /build

# Copy Gradle wrapper files
COPY --chmod=0755 gradlew gradlew
COPY gradle/ gradle/
COPY build.gradle settings.gradle ./

# Download dependencies (cached layer)
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew dependencies --no-daemon

# Copy source code and build
COPY src ./src
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Expose application port
EXPOSE 8080

# Copy the built JAR from builder stage
COPY --from=builder /build/build/libs/*.jar app.jar

# Create non-root user for security
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
