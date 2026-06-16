package com.bk.arenax.infrastructure.exception;

public class AlreadyHasTenantException extends TenantException{
 public AlreadyHasTenantException(){
   super("ACCOUNT_ALREADY_HAS_TENANT","Account đã có đăng ký công ty rồi");
 }
}
