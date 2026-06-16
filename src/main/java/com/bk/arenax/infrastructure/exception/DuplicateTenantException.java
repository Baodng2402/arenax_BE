package com.bk.arenax.infrastructure.exception;

public class DuplicateTenantException extends TenantException{
  public DuplicateTenantException(String companyName) {
    super("TENANT_DUPLICATED","Tên Doanh nghiệp đã tồn tại: "+companyName);
  }
}
