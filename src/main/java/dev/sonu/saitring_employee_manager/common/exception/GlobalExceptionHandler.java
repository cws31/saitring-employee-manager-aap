
package dev.sonu.saitring_employee_manager.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.HttpMediaTypeNotSupportedException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(
            AppException ex,
            HttpServletRequest request) {

        HttpStatus status = HttpStatus.valueOf(ex.getStatus().value());

        if (status.is5xxServerError()) {
            log.error("Application exception on URI [{}], status [{}], traceId [{}]: {}",
                    request.getRequestURI(), status.value(), org.slf4j.MDC.get("traceId"), ex.getMessage(), ex);
        } else {
            log.warn("Application exception on URI [{}], status [{}], traceId [{}]: {}",
                    request.getRequestURI(), status.value(), org.slf4j.MDC.get("traceId"), ex.getMessage());
        }

        ErrorResponse body = ErrorResponse.of(
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI());

        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> fieldErrors = new LinkedHashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {

            fieldErrors.putIfAbsent(
                    fieldError.getField(),
                    fieldError.getDefaultMessage());
        }

        log.warn("Validation failure on URI [{}], fields [{}]",
                request.getRequestURI(), fieldErrors.keySet());

        ErrorResponse body = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Validation failed for one or more fields",
                request.getRequestURI(),
                fieldErrors);

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(
            BindException ex,
            HttpServletRequest request) {

        Map<String, String> fieldErrors = new LinkedHashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {

            fieldErrors.putIfAbsent(
                    fieldError.getField(),
                    fieldError.getDefaultMessage());
        }

        log.warn("Request binding failure on URI [{}], fields [{}]",
                request.getRequestURI(), fieldErrors.keySet());

        ErrorResponse body = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Request binding or validation failed",
                request.getRequestURI(),
                fieldErrors);

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedRequest(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        log.warn("Malformed request body on URI [{}]",
                request.getRequestURI());

        ErrorResponse body = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Request body is malformed or contains invalid values",
                request.getRequestURI());

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponse> handleMissingMultipartPart(
            MissingServletRequestPartException ex,
            HttpServletRequest request) {

        log.warn("Missing multipart part on URI [{}]",
                request.getRequestURI());

        ErrorResponse body = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "A required multipart request part is missing",
                request.getRequestURI());

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(
            MissingServletRequestParameterException ex,
            HttpServletRequest request) {

        ErrorResponse body = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "A required request parameter is missing",
                request.getRequestURI());

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(
            HttpMediaTypeNotSupportedException ex,
            HttpServletRequest request) {

        ErrorResponse body = ErrorResponse.of(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
                HttpStatus.UNSUPPORTED_MEDIA_TYPE.getReasonPhrase(),
                "The request content type is not supported",
                request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request) {

        log.warn("Constraint violation on URI [{}]",
                request.getRequestURI());

        ErrorResponse body = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "One or more request constraints were violated",
                request.getRequestURI());

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request) {

        log.warn("Authentication failure on URI [{}]",
                request.getRequestURI());

        ErrorResponse body = ErrorResponse.of(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "Authentication failed",
                request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request) {

        log.warn("Access denied on URI [{}]",
                request.getRequestURI());

        ErrorResponse body = ErrorResponse.of(
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                "Access is denied",
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException ex,
            HttpServletRequest request) {

        log.warn("Upload size limit exceeded on URI [{}]",
                request.getRequestURI());

        ErrorResponse body = ErrorResponse.of(
                HttpStatus.PAYLOAD_TOO_LARGE.value(),
                HttpStatus.PAYLOAD_TOO_LARGE.getReasonPhrase(),
                "File size exceeds the maximum permitted upload limit",
                request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {

        log.warn("Database integrity constraint violation on URI [{}]",
                request.getRequestURI());

        ErrorResponse body = ErrorResponse.of(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                "The request conflicts with existing data or a database constraint",
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler({org.springframework.dao.InvalidDataAccessApiUsageException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleInvalidQueryParameter(
            Exception ex,
            HttpServletRequest request) {

        log.warn("Invalid query parameter on URI [{}]: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse body = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Invalid query or sorting parameter: " + ex.getMessage(),
                request.getRequestURI());

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(
            Exception ex,
            HttpServletRequest request) {

        log.error("Unhandled exception processing URI [{}], traceId [{}]: {}",
                request.getRequestURI(),
                org.slf4j.MDC.get("traceId"),
                ex.getMessage(),
                ex);

        ErrorResponse body = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "An unexpected internal error occurred. Please try again later.",
                request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body);
    }
}
