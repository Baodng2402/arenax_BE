package com.bk.arenax.identity.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bk.arenax.identity.dto.request.LoginRequest;
import com.bk.arenax.identity.dto.request.PasswordResetRequest;
import com.bk.arenax.identity.dto.request.RegisterRequest;
import com.bk.arenax.identity.dto.request.ResetPasswordRequest;
import com.bk.arenax.identity.dto.request.VerifyEmailRequest;
import com.bk.arenax.identity.dto.response.AuthTokenResponse;
import com.bk.arenax.identity.dto.response.RegisterResponse;
import com.bk.arenax.identity.infrastructure.security.CookieProperties;
import com.bk.arenax.identity.service.AuthenticationService;
import com.bk.arenax.identity.service.PasswordResetService;
import com.bk.arenax.identity.service.RegistrationService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthenticationService authenticationService;
  private final RegistrationService registrationService;
  private final PasswordResetService passwordResetService;
  private final CookieProperties cookieProperties;

  @PostMapping("/register")
  public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
    var user =
        registrationService.register(request.email(), request.password(), request.fullName());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new RegisterResponse(user.getId(), user.getEmail(), user.getStatus().name()));
  }

  @PostMapping("/verify-email")
  public ResponseEntity<Void> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
    registrationService.verifyEmail(request.token());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/login")
  public ResponseEntity<AuthTokenResponse> login(@Valid @RequestBody LoginRequest request) {
    AuthenticationService.LoginResult result =
        authenticationService.login(request.email(), request.password(), request.accountId());
    return withRefreshCookie(result);
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthTokenResponse> refresh(
      @CookieValue("arenax_refresh_token") String refreshToken) {
    AuthenticationService.LoginResult result = authenticationService.refresh(refreshToken);
    return withRefreshCookie(result);
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@CookieValue("arenax_refresh_token") String refreshToken) {
    authenticationService.logout(refreshToken);
    return clearRefreshCookie();
  }

  @PostMapping("/logout-all")
  public ResponseEntity<Void> logoutAll(
      JwtAuthenticationToken authentication,
      @CookieValue(value = "arenax_refresh_token", required = false) String refreshToken) {
    authenticationService.logoutAll(UUID.fromString(authentication.getToken().getSubject()));
    if (refreshToken != null && !refreshToken.isBlank()) {
      authenticationService.logout(refreshToken);
    }
    return clearRefreshCookie();
  }

  @PostMapping("/request-password-reset")
  public ResponseEntity<Void> requestPasswordReset(
      @Valid @RequestBody PasswordResetRequest request) {
    passwordResetService.requestPasswordReset(request.email());
    return ResponseEntity.accepted().build();
  }

  @PostMapping("/reset-password")
  public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    passwordResetService.resetPassword(request.token(), request.newPassword());
    return clearRefreshCookie();
  }

  private ResponseEntity<AuthTokenResponse> withRefreshCookie(
      AuthenticationService.LoginResult result) {
    ResponseCookie refreshCookie =
        ResponseCookie.from("arenax_refresh_token", result.refreshToken())
            .httpOnly(true)
            .secure(cookieProperties.secure())
            .sameSite("Strict")
            .path("/api/v1/auth")
            .maxAge(authenticationService.refreshTokenTtlSeconds())
            .build();
    return ResponseEntity.ok()
        .header("Set-Cookie", refreshCookie.toString())
        .body(result.response());
  }

  private ResponseEntity<Void> clearRefreshCookie() {
    ResponseCookie refreshCookie =
        ResponseCookie.from("arenax_refresh_token", "")
            .httpOnly(true)
            .secure(cookieProperties.secure())
            .sameSite("Strict")
            .path("/api/v1/auth")
            .maxAge(0)
            .build();
    return ResponseEntity.noContent().header("Set-Cookie", refreshCookie.toString()).build();
  }
}
