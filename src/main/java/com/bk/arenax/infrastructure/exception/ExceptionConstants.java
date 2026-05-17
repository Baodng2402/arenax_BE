package com.bk.arenax.infrastructure.exception;

public final class ExceptionConstants {
    private ExceptionConstants() {
    }

    public static final String USER_NOT_FOUND = "USER_NOT_FOUND";
    public static final String EMAIL_ALREADY_EXISTS = "EMAIL_ALREADY_EXISTS";
    public static final String BAD_CREDENTIALS = "BAD_CREDENTIALS";
    public static final String ACCESS_DENIED = "ACCESS_DENIED";
    public static final String VALIDATION_FAILED = "VALIDATION_FAILED";
    public static final String CONSTRAINT_VIOLATION = "CONSTRAINT_VIOLATION";
    public static final String DATA_INTEGRITY_VIOLATION = "DATA_INTEGRITY_VIOLATION";
    public static final String BAD_REQUEST = "BAD_REQUEST";
    public static final String MESSAGE_NOT_READABLE = "MESSAGE_NOT_READABLE";
    public static final String UNEXPECTED_ERROR = "UNEXPECTED_ERROR";

    public static final String INVALID_EMAIL_OR_PASSWORD_MESSAGE = "Invalid email or password";
    public static final String ACCESS_DENIED_MESSAGE = "Access denied";
    public static final String DATA_INTEGRITY_VIOLATION_MESSAGE = "Data integrity violation";
    public static final String MESSAGE_NOT_READABLE_MESSAGE = "Request body is missing or invalid";
    public static final String UNEXPECTED_ERROR_MESSAGE = "Unexpected error";
}
