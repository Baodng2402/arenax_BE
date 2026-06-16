package com.bk.arenax.infrastructure.exception;

public abstract class TenantException extends RuntimeException {
  private final String code;
  public TenantException(String code,String message) {
    super(message);
    this.code = code;
  }
  public String getCode(){
    return code;
  }
}
