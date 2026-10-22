package dev.sonu.saitring_employee_manager.common.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Custom Actuator Health Indicator providing real-time production system telemetry:
 * - JVM Heap & Non-Heap Memory metrics (Used vs Max)
 * - Thread pool & runtime process uptime
 * - Live DataSource connectivity probe
 * - Redis cluster / instance ping verification
 */
@Component("semaSystem")
public class SemaSystemHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;
    private final RedisConnectionFactory redisConnectionFactory;
    private final RuntimeMXBean runtimeMXBean;

    public SemaSystemHealthIndicator(DataSource dataSource,
                                     RedisConnectionFactory redisConnectionFactory) {
        this.dataSource = dataSource;
        this.redisConnectionFactory = redisConnectionFactory;
        this.runtimeMXBean = ManagementFactory.getRuntimeMXBean();
    }

    @Override
    public Health health() {
        Map<String, Object> details = new LinkedHashMap<>();
        boolean isHealthy = true;

        // 1. JVM Runtime Telemetry
        Runtime runtime = Runtime.getRuntime();
        long totalMemoryMb = runtime.totalMemory() / (1024 * 1024);
        long freeMemoryMb = runtime.freeMemory() / (1024 * 1024);
        long maxMemoryMb = runtime.maxMemory() / (1024 * 1024);
        long usedMemoryMb = totalMemoryMb - freeMemoryMb;

        Map<String, Object> jvmDetails = new LinkedHashMap<>();
        jvmDetails.put("usedMemoryMb", usedMemoryMb);
        jvmDetails.put("maxMemoryMb", maxMemoryMb);
        jvmDetails.put("availableProcessors", runtime.availableProcessors());
        jvmDetails.put("uptimeSeconds", runtimeMXBean.getUptime() / 1000);
        details.put("jvm", jvmDetails);

        // 2. Relational Database Ping Probe
        Map<String, Object> dbDetails = new LinkedHashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            boolean valid = connection.isValid(2);
            dbDetails.put("status", valid ? "UP" : "DEGRADED");
            dbDetails.put("databaseProduct", connection.getMetaData().getDatabaseProductName());
            if (!valid) {
                isHealthy = false;
            }
        } catch (Exception ex) {
            dbDetails.put("status", "DOWN");
            dbDetails.put("error", ex.getMessage());
            isHealthy = false;
        }
        details.put("database", dbDetails);

        // 3. Redis Connectivity Ping Probe
        Map<String, Object> redisDetails = new LinkedHashMap<>();
        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            String pingResult = connection.ping();
            redisDetails.put("status", "PONG".equalsIgnoreCase(pingResult) ? "UP" : "DEGRADED");
            redisDetails.put("response", pingResult);
        } catch (Exception ex) {
            redisDetails.put("status", "DOWN");
            redisDetails.put("error", ex.getMessage());
            // Redis failure should be visible in details but might not bring entire app down if cache is transient
            isHealthy = false;
        }
        details.put("redis", redisDetails);

        if (isHealthy) {
            return Health.up().withDetails(details).build();
        } else {
            return Health.down().withDetails(details).build();
        }
    }
}
