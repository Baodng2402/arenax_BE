package com.bk.arenax.identity.controller;

import com.bk.arenax.identity.controller.dto.AuthTokenResponse;
import com.bk.arenax.identity.controller.dto.LoginRequest;
import com.bk.arenax.identity.controller.dto.PasswordResetRequest;
import com.bk.arenax.identity.controller.dto.RegisterRequest;
import com.bk.arenax.identity.controller.dto.RegisterResponse;
import com.bk.arenax.identity.controller.dto.ResetPasswordRequest;
import com.bk.arenax.identity.controller.dto.VerifyEmailRequest;
import com.bk.arenax.identity.infrastructure.security.CookieProperties;
import com.bk.arenax.identity.service.UserService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final CookieProperties cookieProperties;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        var user = userService.register(request.email(), request.password(), request.fullName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RegisterResponse(user.getId(), user.getEmail(), user.getStatus().name()));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        userService.verifyEmail(request.token());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthTokenResponse> login(@Valid @RequestBody LoginRequest request) {
        UserService.LoginResult result = userService.login(request.email(), request.password(), request.accountId());
        return withRefreshCookie(result);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthTokenResponse> refresh(@CookieValue("arenax_refresh_token") String refreshToken) {
        UserService.LoginResult result = userService.refresh(refreshToken);
        return withRefreshCookie(result);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue("arenax_refresh_token") String refreshToken) {
        userService.logout(refreshToken);
        return clearRefreshCookie();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(
            JwtAuthenticationToken authentication,
            @CookieValue(value = "arenax_refresh_token", required = false) String refreshToken) {
        userService.logoutAll(UUID.fromString(authentication.getToken().getSubject()));
        if (refreshToken != null && !refreshToken.isBlank()) {
            userService.logout(refreshToken);
        }
        return clearRefreshCookie();
    }

    @PostMapping("/request-password-reset")
    public ResponseEntity<Void> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        userService.requestPasswordReset(request.email());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.token(), request.newPassword());
        return clearRefreshCookie();
    }

    private ResponseEntity<AuthTokenResponse> withRefreshCookie(UserService.LoginResult result) {
        ResponseCookie refreshCookie = ResponseCookie.from("arenax_refresh_token", result.refreshToken())
                .httpOnly(true)
                .secure(cookieProperties.secure())
                .sameSite("Strict")
                .path("/api/v1/auth")
                .maxAge(userService.refreshTokenTtlSeconds())
                .build();
        return ResponseEntity.ok()
                .header("Set-Cookie", refreshCookie.toString())
                .body(result.response());
    }

    private ResponseEntity<Void> clearRefreshCookie() {
        ResponseCookie refreshCookie = ResponseCookie.from("arenax_refresh_token", "")
                .httpOnly(true)
                .secure(cookieProperties.secure())
                .sameSite("Strict")
                .path("/api/v1/auth")
                .maxAge(0)
                .build();
        return ResponseEntity.noContent()
                .header("Set-Cookie", refreshCookie.toString())
                .build();
    }
}
