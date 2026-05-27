package com.bk.arenax.infrastructure.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

  // Generic
  ENTITY_NOT_FOUND("ARENAX.EX.001", "Entity not found", HttpStatus.NOT_FOUND),
  EMAIL_ALREADY_EXISTS("ARENAX.EX.002", "Email already exists", HttpStatus.CONFLICT),
  BAD_REQUEST("ARENAX.EX.003", "Bad request", HttpStatus.BAD_REQUEST),

  // Auth
  BAD_CREDENTIALS("ARENAX.AUTH.001", "Invalid email or password", HttpStatus.UNAUTHORIZED),
  INVALID_JWT_TOKEN("ARENAX.AUTH.002", "The token is invalid", HttpStatus.UNAUTHORIZED),
  INVALID_REFRESH_TOKEN("ARENAX.AUTH.003", "Invalid refresh token", HttpStatus.UNAUTHORIZED),

  // User
  USER_NOT_FOUND("ARENAX.USR.001", "User not found", HttpStatus.NOT_FOUND),

  // Validation / Request parsing
  VALIDATION_FAILED("ARENAX.VAL.001", "Validation failed", HttpStatus.BAD_REQUEST),
  MESSAGE_NOT_READABLE(
      "ARENAX.VAL.002", "Request body is missing or invalid", HttpStatus.BAD_REQUEST),
  CONSTRAINT_VIOLATION("ARENAX.VAL.003", "Constraint violation", HttpStatus.BAD_REQUEST),

  // Database
  DATA_INTEGRITY_VIOLATION("ARENAX.DB.001", "Data integrity violation", HttpStatus.CONFLICT),

  // Security / Authorization
  ACCESS_DENIED("ARENAX.SEC.001", "Access denied", HttpStatus.FORBIDDEN),

  // Internal
  UNEXPECTED_ERROR("ARENAX.SYS.999", "Unexpected error", HttpStatus.INTERNAL_SERVER_ERROR);

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;
}
