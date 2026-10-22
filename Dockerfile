# ==============================================================================
# Build Stage
# ==============================================================================
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Cache maven dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Compile and package application
COPY src ./src
RUN mvn clean package -DskipTests -B

# ==============================================================================
# Runtime Stage
# ==============================================================================
FROM eclipse-temurin:21-jre-alpine AS runner

WORKDIR /app

# Install curl for healthcheck probe
RUN apk --no-cache add curl

# Add unprivileged runtime user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder /build/target/saitring-employee-manager-*.jar /app/app.jar

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseG1GC", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+ExitOnOutOfMemoryError", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "/app/app.jar"]
