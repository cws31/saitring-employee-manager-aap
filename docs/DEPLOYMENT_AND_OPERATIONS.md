# Deployment, Operations & Production Readiness Guide

## 1. Environment Configuration Reference

The application follows the **Twelve-Factor App methodology**. All critical operational parameters are configured through environment variables or an optional root `.env` file (`spring.config.import=optional:file:.env[.properties]`).

### 1.1 Complete Environment Variables Matrix

| Variable Name | Required | Default Value | Description |
|---|---|---|---|
| `APP_NAME` | No | `saitring-employee-manager` | Spring application name identifier |
| `DB_URL` | **Yes** | `jdbc:mysql://localhost:3306/saitring_db` | MySQL JDBC connection string with parameters |
| `DB_USERNAME` | **Yes** | - | Database authentication username |
| `DB_PASSWORD` | **Yes** | - | Database authentication password |
| `UPSTASH_REDIS_URL` | **Yes** | `rediss://default:...@...upstash.io:6379` | Redis / Valkey connection URI (supports SSL/TLS) |
| `APP_JWT_SECRET` | **Yes** | - | Base64 or 256-bit secret key for HMAC-SHA256 signing |
| `APP_JWT_EXPIRATION_SECONDS`| No | `86400` (24 Hours) | JWT access token validity period |
| `BREVO_API_KEY` | Optional | - | Brevo API key for transactional Email/SMS OTP dispatch |
| `BREVO_SENDER_EMAIL` | No | `sema.servicesupport@gmail.com` | Verified Brevo sender address |
| `BREVO_SENDER_NAME` | No | `Saitring Security` | Sender display name in OTP emails |
| `TWILIO_ACCOUNT_SID` | Optional | - | Twilio Account SID for SMS OTP dispatch |
| `TWILIO_AUTH_TOKEN` | Optional | - | Twilio Auth Token |
| `TWILIO_FROM_NUMBER` | Optional | - | Twilio registered phone number |
| `SUPABASE_URL` | Optional | `https://phquzmzdfdfbvosexgct.supabase.co` | Supabase project API URL |
| `SUPABASE_KEY` | Optional | - | Supabase service role key for storage upload |
| `SUPABASE_BUCKET` | No | `organization-logos` | Target storage bucket for organization logos |
| `S3_BUCKET` | Optional | `sema-31` | AWS S3 bucket name (alternative asset storage) |
| `AWS_REGION` | No | `eu-north-1` | AWS region code |
| `CORS_ALLOWED_ORIGINS` | No | `http://localhost:3000,http://localhost:5173`| Comma-separated CORS allowed origin URLs |

---

## 2. Java Runtime & JVM Sizing

SEMA runs on **Java 21 LTS** (and is verified on Java 25).

### 2.1 Recommended JVM Flags for Production Container
```bash
java -server \
     -XX:+UseG1GC \
     -XX:MaxRAMPercentage=75.0 \
     -XX:+ExitOnOutOfMemoryError \
     -XX:+HeapDumpOnOutOfMemoryError \
     -XX:HeapDumpPath=/var/log/heapdump.hprof \
     -Djava.security.egd=file:/dev/./urandom \
     -jar target/saitring-employee-manager-0.0.1-SNAPSHOT.jar
```

---

## 3. HikariCP Connection Pool Optimization

The application relies on HikariCP for high-performance connection pooling.

```properties
spring.datasource.hikari.maximum-pool-size=15
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.max-lifetime=1200000
spring.datasource.hikari.leak-detection-threshold=15000
```

> [!TIP]
> Keep connection pools lean. Setting `maximum-pool-size` beyond database CPU and IOPS capabilities leads to connection queueing and thread thrashing.

---

## 4. Observability, Health & Telemetry

### 4.1 Spring Boot Actuator Endpoints

| Endpoint | Security | Purpose |
|---|---|---|
| `GET /actuator/health` | Public | Kubernetes liveness and readiness probe |
| `GET /actuator/info` | Public | Application metadata, build version, and active profiles |
| `GET /actuator/metrics` | Authenticated | JVM heap, garbage collection, and HTTP request metrics |

### 4.2 Logging Configuration
The application outputs standardized, structured log messages with correlated trace IDs:

```properties
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%X{traceId:-NO_TRACE}] %-5level %logger{36} - %msg%n
```

---

## 5. Production Pre-Flight Checklist

1. [ ] **Flyway Validation**: Verify Flyway migrations run cleanly against the target MySQL database (`spring.flyway.enabled=true`).
2. [ ] **JPA DDL Validation**: Ensure Hibernate DDL auto-alteration is set strictly to `validate` (`spring.jpa.hibernate.ddl-auto=validate`).
3. [ ] **JWT Key Entropy**: Verify `APP_JWT_SECRET` contains at least 32 cryptographically random alphanumeric characters (256 bits).
4. [ ] **Redis High Availability**: Ensure Redis endpoint has TLS enabled and latency $< 10\text{ms}$.
5. [ ] **CORS Lockdown**: Replace `localhost` origins in `CORS_ALLOWED_ORIGINS` with the production frontend domain names.
6. [ ] **Error Obfuscation**: Confirm stack traces and internal binding messages are disabled (`server.error.include-stacktrace=never`).
