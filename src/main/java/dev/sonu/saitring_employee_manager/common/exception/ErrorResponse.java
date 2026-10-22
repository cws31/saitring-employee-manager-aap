package dev.sonu.saitring_employee_manager.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.slf4j.MDC;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private final Instant timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final String traceId;
    private final Map<String, String> fieldErrors;

    public ErrorResponse(Instant timestamp, int status, String error, String message, String path, String traceId, Map<String, String> fieldErrors) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.traceId = traceId;
        this.fieldErrors = fieldErrors;
    }

    public ErrorResponse(Instant timestamp, int status, String error, String message, String path, Map<String, String> fieldErrors) {
        this(timestamp, status, error, message, path, MDC.get("traceId"), fieldErrors);
    }

    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(Instant.now(), status, error, message, path, MDC.get("traceId"), null);
    }

    public static ErrorResponse of(int status, String error, String message, String path, Map<String, String> fieldErrors) {
        return new ErrorResponse(Instant.now(), status, error, message, path, MDC.get("traceId"), fieldErrors);
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public String getTraceId() {
        return traceId;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
