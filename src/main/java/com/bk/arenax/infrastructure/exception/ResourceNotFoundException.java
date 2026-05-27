package com.bk.arenax.infrastructure.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ArenaXException {
  public ResourceNotFoundException(ErrorCode errorCode) {
    super(errorCode);
  }

  public ResourceNotFoundException(ErrorCode errorCode, String message) {
    super(errorCode, message);
  }

  public ResourceNotFoundException(String code, String message) {
    super(code, message, HttpStatus.NOT_FOUND);
  }
}
