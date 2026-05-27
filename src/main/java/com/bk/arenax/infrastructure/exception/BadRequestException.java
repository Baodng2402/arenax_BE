package com.bk.arenax.infrastructure.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends ArenaXException {
  public BadRequestException(ErrorCode errorCode) {
    super(errorCode);
  }

  public BadRequestException(ErrorCode errorCode, String message) {
    super(errorCode, message);
  }

  public BadRequestException(String code, String message) {
    super(code, message, HttpStatus.BAD_REQUEST);
  }
}
