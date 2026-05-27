package com.bk.arenax.infrastructure.security;

public class SecurityConstants {
  private SecurityConstants() {}

  public static final String[] PUBLIC_APIS = {
    "/",
    "/swagger-ui.html",
    "/swagger-ui/**",
    "/v3/api-docs/**",
    "/api-docs/**",
    "/actuator/health",
    "/actuator/info",
    "/api/*/auth/register/platform",
    "/api/*/auth/login",
    "/api/*/auth/login/social",
    "/api/*/auth/simple-link-login",
    "/api/*/auth/verify-email",
    "/api/*/auth/accept-invitation",
    "/api/*/auth/find-id/send-code",
    "/api/*/auth/find-id/verify-code",
    "/api/*/auth/find-password/send-code",
    "/api/*/auth/find-password/verify-code",
    "/api/*/auth/reset-password",
    "/api/*/auth/register/supplier",
    "/api/*/auth/refresh",
  };

  // CORS configuration constants
  public static final String CORS_PATH_PATTERNS = "/**";
  public static final String[] CORS_ALLOWED_HEADERS = {"*"};
  public static final String[] CORS_EXPOSED_HEADERS = {"Authorization", "Content-Type", "Accept"};
  public static final String[] CORS_ALLOWED_METHODS = {
    "GET", "PUT", "POST", "DELETE", "PATCH", "OPTIONS"
  };
}
