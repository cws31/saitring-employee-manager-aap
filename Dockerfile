
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B


FROM eclipse-temurin:21-jre-alpine AS runner

WORKDIR /app

RUN apk --no-cache add curl \
    && addgroup -S appgroup \
    && adduser -S appuser -G appgroup

COPY --from=builder /build/target/saitring-employee-manager-*.jar /app/app.jar

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=120s --retries=3 \
  CMD curl -fsS http://localhost:8080/actuator/health || exit 1

ENV JAVA_OPTS="-XX:+UseSerialGC -Xms24m -Xmx128m -XX:MaxMetaspaceSize=128m -XX:ReservedCodeCacheSize=24m -Xss256k -XX:+ExitOnOutOfMemoryError -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
