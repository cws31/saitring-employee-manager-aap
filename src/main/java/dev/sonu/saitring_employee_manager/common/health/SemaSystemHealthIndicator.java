package dev.sonu.saitring_employee_manager.common.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Custom Actuator Health Indicator providing real-time production system telemetry:
 * - JVM Heap & Non-Heap Memory metrics (Used vs Free vs Max in MB)
 * - Thread pool & runtime process uptime
 * - Available CPU cores
 *
 * (Database and Redis connectivity are monitored as dedicated first-class
 * health indicators by Spring Boot Actuator's 'db' and 'redis' components).
 */
@Component("semaSystem")
public class SemaSystemHealthIndicator implements HealthIndicator {

    private final RuntimeMXBean runtimeMXBean;

    public SemaSystemHealthIndicator() {
        this.runtimeMXBean = ManagementFactory.getRuntimeMXBean();
    }

    @Override
    public Health health() {
        Map<String, Object> details = new LinkedHashMap<>();

        // JVM Runtime Telemetry
        Runtime runtime = Runtime.getRuntime();
        long totalMemoryMb = runtime.totalMemory() / (1024 * 1024);
        long freeMemoryMb = runtime.freeMemory() / (1024 * 1024);
        long maxMemoryMb = runtime.maxMemory() / (1024 * 1024);
        long usedMemoryMb = totalMemoryMb - freeMemoryMb;

        Map<String, Object> jvmDetails = new LinkedHashMap<>();
        jvmDetails.put("usedMemoryMb", usedMemoryMb);
        jvmDetails.put("freeMemoryMb", freeMemoryMb);
        jvmDetails.put("totalMemoryMb", totalMemoryMb);
        jvmDetails.put("maxMemoryMb", maxMemoryMb);
        jvmDetails.put("availableProcessors", runtime.availableProcessors());
        jvmDetails.put("uptimeSeconds", runtimeMXBean.getUptime() / 1000);
        details.put("jvm", jvmDetails);

        return Health.up().withDetails(details).build();
    }
}
