package com.bk.arenax.identity.infrastructure.security;

public final class SecurityEndpoints {

  public static final String[] PUBLIC = {
          "/api/v1/auth/register",
          "/api/v1/auth/login",
          "/actuator/health"
  };

  private SecurityEndpoints(){}
}
