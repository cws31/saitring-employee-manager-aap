# Production Observability, Health Monitoring & Distributed Tracing

## 1. Overview & Observability Architecture

The **Saitring Employee Manager (SEMA)** backend incorporates enterprise-grade observability to ensure that operations and engineering teams can track every inbound request in real-time, monitor system health, and instantly isolate and fix production incidents.

```mermaid
flowchart TD
    Client["Client / Frontend"] -->|1. Request (Optional X-Trace-Id)| TraceFilter["TraceIdFilter<br/>(Order: HIGHEST_PRECEDENCE)"]
    TraceFilter -->|2. Bind UUID to MDC & Response Header| SecFilter["JwtAuthenticationFilter"]
    SecFilter -->|3. Bind Tenant ID to MDC [tenant:ownerId]| AppLogic["Domain Services & Repositories"]
    AppLogic -->|4. Structured Logging with [traceId] [tenant:id]| LogStream["Logback Console Stream<br/>(ELK / CloudWatch / Loki)"]
    
    AppLogic -.->|Error / Exception Occurs| ErrHandler["GlobalExceptionHandler"]
    ErrHandler -->|5. Logs Stack Trace with TraceId| LogStream
    ErrHandler -->|6. Emits RFC 7807 JSON with traceId| Client

    Monitor["Prometheus / Datadog / K8s"] -->|Poll /actuator/health| Actuator["Spring Boot Actuator"]
    Monitor -->|Scrape /actuator/prometheus| Actuator
```

---

## 2. Distributed Request Tracing & Correlation IDs

Every HTTP request passing through the system is assigned a unique correlation ID (`traceId`).

### 2.1 Trace ID Flow
1. **Inbound Resolution**:
   - If the client transmits `X-Trace-Id` or `X-Request-Id` (e.g., from an API gateway, reverse proxy, or web frontend), SEMA preserves and binds that exact ID.
   - If omitted, `TraceIdFilter` automatically generates a cryptographically random UUIDv4.
2. **Mapped Diagnostic Context (MDC)**:
   - The trace ID is placed into the SLF4J MDC under the key `traceId`.
   - Once authenticated via JWT, the tenant identity is bound under `ownerId` (e.g., `tenant:42`).
   - Every log statement executed during that request thread automatically prepends:
     ```
     [timestamp] [thread] [traceId] [tenant:ownerId] level logger - message
     ```
3. **Outbound Response Header**:
   - The server injects the header `X-Trace-Id: <traceId>` into every HTTP response.
4. **Client Error Body Injection**:
   - If a request fails with an error ($4xx$ or $5xx$), the structured JSON response contains the `traceId`:
     ```json
     {
       "timestamp": "2026-10-22T23:12:00Z",
       "status": 500,
       "error": "Internal Server Error",
       "message": "An unexpected internal error occurred. Please try again later.",
       "path": "/api/v1/hisab/settle",
       "traceId": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d"
     }
     ```

---

## 3. Production Incident Investigation Guide

When an error is reported by a user or captured in monitoring, follow this rapid 3-step investigation process:

### Step 1: Capture the Trace ID
Obtain the trace ID from either:
- The HTTP response header: `X-Trace-Id`
- The JSON error body: `$.traceId`
- An alert fired by your monitoring dashboard or APM tool.

### Step 2: Query the Logs
Search your log aggregation system (AWS CloudWatch, Datadog, Grafana Loki, or Elasticsearch / Kibana):

```bash
# Query by Trace ID in terminal or log collector
grep "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d" /var/log/sema/application.log
```

### Step 3: Analyze Correlated Execution Sequence
Because the `traceId` spans the entire request thread, the log output immediately reconstructs the full narrative:

```text
2026-10-22 23:12:00.100 [http-nio-8080-exec-4] [9b1deb4d...] [ANON] INFO  TraceIdFilter - HTTP INBOUND [POST] [/api/v1/hisab/settle] from IP [198.51.100.24]
2026-10-22 23:12:00.115 [http-nio-8080-exec-4] [9b1deb4d...] [tenant:12] INFO  JwtAuthenticationFilter - Authenticated owner tenant [12]
2026-10-22 23:12:00.125 [http-nio-8080-exec-4] [9b1deb4d...] [tenant:12] INFO  HisabServiceImpl - Initiating monthly hisab settlement for employee [34] in month [2026-10]
2026-10-22 23:12:00.140 [http-nio-8080-exec-4] [9b1deb4d...] [tenant:12] ERROR GlobalExceptionHandler - Unhandled exception processing URI [/api/v1/hisab/settle], traceId [9b1deb4d...]: Connection timeout to payment gateway
java.net.SocketTimeoutException: Read timed out
    at dev.sonu.saitring_employee_manager.hisab.service.HisabServiceImpl.settleHisab(HisabServiceImpl.java:620)
2026-10-22 23:12:00.145 [http-nio-8080-exec-4] [9b1deb4d...] [tenant:12] ERROR TraceIdFilter - HTTP OUTBOUND [POST] [/api/v1/hisab/settle] -> Status [500] in [45ms]
```

**Benefits**:
* Zero ambiguity: You instantly identify the exact tenant (`[tenant:12]`), client IP, method, execution duration (`45ms`), and full stack trace.
* No sensitive tokens or passwords leaked into logs.

---

## 4. Health Check & Monitoring Endpoints

All actuator endpoints are configured in `application.properties` and integrated with Spring Security.

| Endpoint | Access | Response Format | Purpose |
|---|---|---|---|
| `GET /actuator/health` | **Public** | JSON | Aggregated system health including DB, Redis, and JVM components |
| `GET /actuator/health/liveness` | **Public** | JSON | Kubernetes / Docker liveness probe (`{"status":"UP"}`) |
| `GET /actuator/health/readiness`| **Public** | JSON | Kubernetes / Docker readiness probe (verifies DB & Redis connections) |
| `GET /actuator/info` | **Public** | JSON | Application build version, environment, and runtime metadata |
| `GET /actuator/metrics` | Authenticated | JSON | Metric keys catalog (JVM, CPU, HikariCP, HTTP requests) |
| `GET /actuator/metrics/{metric.name}` | Authenticated | JSON | Detailed metric values (e.g. `http.server.requests`, `jvm.memory.used`) |
| `GET /actuator/prometheus` | **Public** | Text / Prometheus | Prometheus-formatted scrape endpoint for Grafana dashboards |

---

## 5. Custom System Health Diagnostics (`semaSystem`)

A custom health indicator [`SemaSystemHealthIndicator.java`](file:///d:/DOWNLOADS/freelance-projects/saitring-employee-manager/src/main/java/dev/sonu/saitring_employee_manager/common/health/SemaSystemHealthIndicator.java) provides live operational telemetry on every call to `/actuator/health`:

```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "MySQL",
        "validationQuery": "isValid()"
      }
    },
    "redis": {
      "status": "UP",
      "details": {
        "version": "7.0.0"
      }
    },
    "semaSystem": {
      "status": "UP",
      "details": {
        "jvm": {
          "usedMemoryMb": 184,
          "maxMemoryMb": 2048,
          "availableProcessors": 8,
          "uptimeSeconds": 1420
        },
        "database": {
          "status": "UP",
          "databaseProduct": "MySQL"
        },
        "redis": {
          "status": "UP",
          "response": "PONG"
        }
      }
    }
  }
}
```

---

## 6. Prometheus & Grafana Integration

### 6.1 Prometheus Scrape Configuration
Add the following job to your `prometheus.yml`:

```yaml
scrape_configs:
  - job_name: 'sema-backend'
    metrics_path: '/actuator/prometheus'
    scrape_interval: 15s
    static_configs:
      - targets: ['api.saitring.com:8080']
```

### 6.2 Key Prometheus Metrics to Monitor
* **HTTP Latency**: `http_server_requests_seconds_max{uri!~"/actuator.*"}`
* **HTTP 5xx Error Rate**: `rate(http_server_requests_seconds_count{status=~"5.."}[1m])`
* **Hikari Connection Pool Active Connections**: `hikaricp_connections_active`
* **Hikari Connection Timeout Counts**: `hikaricp_connections_timeout_total`
* **JVM Heap Usage**: `jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}`
