package com.univer.bookcom.exception;

import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;


@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ResponseEntity<Map<String, String>> buildErrorResponse(HttpStatus status,
                                                                   String message) {
        Map<String, String> response = new HashMap<>();
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        response.put("status", String.valueOf(status.value()));
        response.put("error", status.getReasonPhrase());
        response.put("message", message);
        return ResponseEntity.status(status).body(response);
    }

    private ResponseEntity<Map<String, Object>> buildValidationErrorResponse(
            HttpStatus status, String message, Map<String, String> errors) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", System.currentTimeMillis());
        response.put("status", status.value());
        response.put("error", status.getReasonPhrase());
        response.put("message", message);
        response.put("errors", errors);
        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCommentNotFound(CommentNotFoundException ex) {
        log.warn("Comment not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(DuplicateCommentException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateComment(DuplicateCommentException
        ex) {
        log.warn("Duplicate comment: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleBookNotFound(BookNotFoundException ex) {
        log.warn("Book not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFound(UserNotFoundException ex) {
        log.warn("User not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleUserAlreadyExists(UserAlreadyExistsException
        ex) {
        log.warn("User already exists: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleInvalidCredentials(InvalidCredentialsException
        ex) {
        log.warn("Invalid credentials: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(InvalidYearException.class)
    public ResponseEntity<Map<String, String>> handleInvalidYear(InvalidYearException ex) {
        log.warn("Invalid year: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(InvalidStatusException.class)
    public ResponseEntity<Map<String, String>> handleInvalidStatus(InvalidStatusException ex) {
        log.warn("Invalid status: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,
            Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = error instanceof FieldError
                    ? ((FieldError) error).getField() : error.getObjectName();
            errors.put(field, error.getDefaultMessage());
        });
        log.warn("Validation failed: {}", errors);
        return buildValidationErrorResponse(HttpStatus.BAD_REQUEST, "Validation failed", errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String,
            Object>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(cv -> {
            String field = cv.getPropertyPath().toString();
            errors.put(field, cv.getMessage());
        });
        log.warn("Constraint violation: {}", errors);
        return buildValidationErrorResponse(HttpStatus.BAD_REQUEST, "Constraint violation", errors);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String,
            String>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String parameterName = ex.getName() != null ? ex.getName() : "unknown";
        String expectedType = ex.getRequiredType() != null
                ? ex.getRequiredType().getSimpleName() : "undefined type";
        String message = String.format("Invalid parameter '%s' type. Expected: %s",
                parameterName, expectedType);
        log.warn("Type mismatch: {}", message);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAllExceptions(Exception ex) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error. Please contact support"
        );
    }

    @ExceptionHandler(CustomValidationException.class)
    public ResponseEntity<Map<String, Object>> handleCustomValidationException(CustomValidationException ex) {
        log.warn("Custom validation failed: {}", ex.getErrors());
        return buildValidationErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Constraint violation",
                ex.getErrors()
        );
    }
}