
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build


COPY pom.xml .
RUN mvn dependency:go-offline -B


COPY src ./src
RUN mvn clean package -DskipTests -B


FROM eclipse-temurin:21-jre-alpine AS runner

WORKDIR /app


RUN apk --no-cache add curl

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder /build/target/saitring-employee-manager-*.jar /app/app.jar

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1




ENV JAVA_OPTS="-XX:+UseSerialGC -Xms12m -Xmx80m -XX:MaxMetaspaceSize=56m -XX:ReservedCodeCacheSize=12m -Xss256k -XX:+ExitOnOutOfMemoryError -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
