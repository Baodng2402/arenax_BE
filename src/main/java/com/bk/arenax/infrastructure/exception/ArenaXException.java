package com.bk.arenax.infrastructure.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class ArenaXException extends RuntimeException {
  private final String code;
  private final HttpStatus status;

  protected ArenaXException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.code = errorCode.getCode();
    this.status = errorCode.getHttpStatus();
  }

  protected ArenaXException(ErrorCode errorCode, String message) {
    super(message);
    this.code = errorCode.getCode();
    this.status = errorCode.getHttpStatus();
  }

  protected ArenaXException(String code, String message, HttpStatus status) {
    super(message);
    this.code = code;
    this.status = status;
  }
}
