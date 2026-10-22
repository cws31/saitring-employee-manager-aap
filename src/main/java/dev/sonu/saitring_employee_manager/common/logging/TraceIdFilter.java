package dev.sonu.saitring_employee_manager.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter that establishes distributed tracing and request observability.
 * - Resolves or generates a unique traceId per HTTP request.
 * - Binds traceId and default tenant context to SLF4J MDC.
 * - Injects X-Trace-Id into response headers for client error tracking.
 * - Measures request latency and logs structured inbound/outbound telemetry.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TraceIdFilter.class);

    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String MDC_TRACE_ID_KEY = "traceId";
    public static final String MDC_OWNER_ID_KEY = "ownerId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();

        String traceId = resolveTraceId(request);
        MDC.put(MDC_TRACE_ID_KEY, traceId);
        MDC.put(MDC_OWNER_ID_KEY, "ANON");

        response.setHeader(TRACE_ID_HEADER, traceId);

        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String method = request.getMethod();
        String clientIp = resolveClientIp(request);

        boolean isActuator = uri.startsWith("/actuator");
        if (!isActuator) {
            log.info("HTTP INBOUND [{}] [{}{}] from IP [{}]",
                    method,
                    uri,
                    (queryString != null ? "?" + queryString : ""),
                    clientIp);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = System.currentTimeMillis() - startTime;
            int status = response.getStatus();

            if (!isActuator) {
                if (status >= 500) {
                    log.error("HTTP OUTBOUND [{}] [{}] -> Status [{}] in [{}ms]", method, uri, status, durationMs);
                } else if (status >= 400) {
                    log.warn("HTTP OUTBOUND [{}] [{}] -> Status [{}] in [{}ms]", method, uri, status, durationMs);
                } else {
                    log.info("HTTP OUTBOUND [{}] [{}] -> Status [{}] in [{}ms]", method, uri, status, durationMs);
                }
            }

            MDC.clear();
        }
    }

    private String resolveTraceId(HttpServletRequest request) {
        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (!StringUtils.hasText(traceId)) {
            traceId = request.getHeader(REQUEST_ID_HEADER);
        }
        if (!StringUtils.hasText(traceId)) {
            traceId = UUID.randomUUID().toString();
        }
        return traceId;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
