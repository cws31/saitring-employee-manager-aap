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

# Production memory tuning optimized for 512MB container limits (Render Free Tier):
# - SerialGC eliminates G1GC native memory overhead (~50MB saved)
# - Max heap 256MB, Metaspace 112MB, CodeCache 48MB, Thread stack 512KB
ENV JAVA_OPTS="-XX:+UseSerialGC -Xms128m -Xmx256m -XX:MaxMetaspaceSize=112m -XX:ReservedCodeCacheSize=48m -Xss512k -XX:+ExitOnOutOfMemoryError -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
