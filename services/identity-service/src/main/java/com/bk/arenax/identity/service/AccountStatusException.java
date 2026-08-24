package com.bk.arenax.identity.service;

import com.bk.arenax.identity.domain.UserStatus;

public class AccountStatusException extends RuntimeException {

  private final UserStatus status;

  public AccountStatusException(UserStatus status) {
    super(messageFor(status));
    this.status = status;
  }

  public UserStatus getStatus() {
    return status;
  }

  public String getCode() {
    return switch (status) {
      case SUSPENDED -> "ACCOUNT_SUSPENDED";
      case DEACTIVATED -> "ACCOUNT_DEACTIVATED";
      default -> "ACCOUNT_NOT_ACTIVE";
    };
  }

  private static String messageFor(UserStatus status) {
    return switch (status) {
      case SUSPENDED -> "Account is suspended";
      case DEACTIVATED -> "Account is deactivated";
      default -> "Account is not active";
    };
  }
}
