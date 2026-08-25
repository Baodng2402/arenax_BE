package com.bk.arenax.identity.infrastructure.security;

public final class SecurityEndpoints {

  public static final String[] PUBLIC = {
    "/api/v1/auth/register",
    "/api/v1/auth/login",
    "/api/v1/auth/verify-email",
    "/api/v1/auth/refresh",
    "/api/v1/auth/logout",
    "/api/v1/auth/request-password-reset",
    "/api/v1/auth/reset-password",
    "/actuator/health"
  };

  private SecurityEndpoints() {}
}
