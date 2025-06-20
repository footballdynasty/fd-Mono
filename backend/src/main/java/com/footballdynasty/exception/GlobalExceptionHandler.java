package com.footballdynasty.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.sentry.Sentry;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        logger.warn("Resource not found: {} - Request: {}", ex.getMessage(), request.getDescription(false));
        
        // Send to Sentry with lower severity since this is expected behavior
        Sentry.configureScope(scope -> {
            scope.setTag("exception_type", "resource_not_found");
            scope.setTag("endpoint", request.getDescription(false));
            scope.setLevel(io.sentry.SentryLevel.INFO);
            scope.setExtra("request_details", request.getDescription(true));
        });
        Sentry.captureMessage("Resource not found: " + ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            "Resource Not Found",
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        logger.warn("Validation failed: {} validation errors - Request: {}", errors.size(), request.getDescription(false));
        
        // Send validation errors to Sentry with context
        Sentry.configureScope(scope -> {
            scope.setTag("exception_type", "validation_failed");
            scope.setTag("endpoint", request.getDescription(false));
            scope.setLevel(io.sentry.SentryLevel.WARNING);
            scope.setExtra("validation_errors", errors.toString());
            scope.setExtra("error_count", String.valueOf(errors.size()));
            scope.setExtra("request_details", request.getDescription(true));
        });
        Sentry.captureMessage("Validation failed with " + errors.size() + " errors");
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation Failed",
            "Invalid input data",
            LocalDateTime.now(),
            errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        logger.error("Unexpected error occurred: {} - Request: {}", ex.getMessage(), request.getDescription(false), ex);
        
        // Send all unexpected exceptions to Sentry with full context
        Sentry.configureScope(scope -> {
            scope.setTag("exception_type", "unexpected_error");
            scope.setTag("exception_class", ex.getClass().getSimpleName());
            scope.setTag("endpoint", request.getDescription(false));
            scope.setLevel(io.sentry.SentryLevel.ERROR);
            scope.setExtra("request_details", request.getDescription(true));
            scope.setExtra("exception_message", ex.getMessage());
            scope.setExtra("exception_class", ex.getClass().getName());
            
            // Add stack trace info
            if (ex.getStackTrace().length > 0) {
                scope.setExtra("first_stack_frame", ex.getStackTrace()[0].toString());
            }
            
            // Add cause if present
            if (ex.getCause() != null) {
                scope.setExtra("root_cause", ex.getCause().getMessage());
                scope.setExtra("root_cause_class", ex.getCause().getClass().getName());
            }
        });
        
        // Capture the full exception
        Sentry.captureException(ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "An unexpected error occurred",
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    public static class ErrorResponse {
        private int status;
        private String error;
        private String message;
        private LocalDateTime timestamp;
        private Map<String, String> validationErrors;

        public ErrorResponse(int status, String error, String message, LocalDateTime timestamp) {
            this.status = status;
            this.error = error;
            this.message = message;
            this.timestamp = timestamp;
        }

        public ErrorResponse(int status, String error, String message, LocalDateTime timestamp, Map<String, String> validationErrors) {
            this(status, error, message, timestamp);
            this.validationErrors = validationErrors;
        }

        // Getters and Setters
        public int getStatus() { return status; }
        public void setStatus(int status) { this.status = status; }
        
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        
        public Map<String, String> getValidationErrors() { return validationErrors; }
        public void setValidationErrors(Map<String, String> validationErrors) { this.validationErrors = validationErrors; }
    }
}