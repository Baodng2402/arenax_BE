package com.bk.arenax.infrastructure.exception;

import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends ArenaXException {
  public DuplicateResourceException(ErrorCode errorCode) {
    super(errorCode);
  }

  public DuplicateResourceException(ErrorCode errorCode, String message) {
    super(errorCode, message);
  }

  public DuplicateResourceException(String code, String message) {
    super(code, message, HttpStatus.CONFLICT);
  }
}
