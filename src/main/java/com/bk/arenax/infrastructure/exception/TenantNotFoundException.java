package com.bk.arenax.infrastructure.exception;

public class TenantNotFoundException extends TenantException {
  public TenantNotFoundException(Long id) {
    super("TENANT_NOT_FOUND", "Không tìm thấy tenant id=" + id);
  }
}
