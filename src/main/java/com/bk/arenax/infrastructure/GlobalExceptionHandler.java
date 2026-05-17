package com.bk.arenax.infrastructure;

import com.bk.arenax.domain.user.UserNotFoundException;
import com.bk.arenax.dto.response.ApiResponse;
import com.bk.arenax.infrastructure.exception.ArenaXException;
import com.bk.arenax.infrastructure.exception.ExceptionConstants;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private ResponseEntity<Object> wrap(String code, String message, HttpStatus status) {
        return new ResponseEntity<>(ApiResponse.error(code, message), status);
    }

    @ExceptionHandler(ArenaXException.class)
    public ResponseEntity<Object> handleArenaXException(ArenaXException e) {
        log.warn("[{}] {}", e.getCode(), e.getMessage());
        return wrap(e.getCode(), e.getMessage(), e.getStatus());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> handleUserNotFoundException(UserNotFoundException e) {
        log.warn("User not found: {}", e.getMessage());
        return wrap(ExceptionConstants.USER_NOT_FOUND, e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentialsException(BadCredentialsException e) {
        log.warn("Bad credentials: {}", e.getMessage());
        return wrap(
                ExceptionConstants.BAD_CREDENTIALS,
                ExceptionConstants.INVALID_EMAIL_OR_PASSWORD_MESSAGE,
                HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("Access denied: {}", e.getMessage());
        return wrap(
                ExceptionConstants.ACCESS_DENIED,
                ExceptionConstants.ACCESS_DENIED_MESSAGE,
                HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException e) {
        log.warn("Constraint violation: {}", e.getMessage());
        return wrap(ExceptionConstants.CONSTRAINT_VIOLATION, e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.warn("Data integrity violation: {}", e.getMessage());
        return wrap(
                ExceptionConstants.DATA_INTEGRITY_VIOLATION,
                ExceptionConstants.DATA_INTEGRITY_VIOLATION_MESSAGE,
                HttpStatus.CONFLICT);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("Bad request: {}", e.getMessage());
        return wrap(ExceptionConstants.BAD_REQUEST, e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException e) {
        log.error("Unhandled exception", e);
        return wrap(
                ExceptionConstants.UNEXPECTED_ERROR,
                ExceptionConstants.UNEXPECTED_ERROR_MESSAGE,
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            @NonNull MethodArgumentNotValidException e,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        String message = e.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    if (error instanceof FieldError fieldError) {
                        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
                    }
                    return error.getDefaultMessage();
                })
                .collect(Collectors.joining(", "));
        log.warn("Validation failed: {}", message);
        return wrap(ExceptionConstants.VALIDATION_FAILED, message, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            @NonNull HttpMessageNotReadableException e,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        log.warn("Message not readable: {}", e.getMessage());
        return wrap(
                ExceptionConstants.MESSAGE_NOT_READABLE,
                ExceptionConstants.MESSAGE_NOT_READABLE_MESSAGE,
                HttpStatus.BAD_REQUEST);
    }
}
