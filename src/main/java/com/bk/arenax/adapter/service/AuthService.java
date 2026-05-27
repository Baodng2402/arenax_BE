package com.bk.arenax.adapter.service;

import com.bk.arenax.adapter.repository.RefreshTokenRepository;
import com.bk.arenax.domain.user.RefreshToken;
import com.bk.arenax.domain.user.User;
import com.bk.arenax.dto.request.LoginRequest;
import com.bk.arenax.dto.request.RefreshTokenRequest;
import com.bk.arenax.dto.response.AuthResponse;
import com.bk.arenax.infrastructure.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
  private final AuthenticationManager authenticationManager;
  private final UserDetailsService userDetailsService;
  private final JwtService jwtService;
  private final RefreshTokenRepository refreshTokenRepository;

  @Transactional
  public AuthResponse login(LoginRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.email(), request.password()));
    User user = (User) userDetailsService.loadUserByUsername(request.email());
    return issueTokens(user);
  }

  @Transactional
  public AuthResponse refresh(RefreshTokenRequest request) {
    RefreshToken storedToken =
        refreshTokenRepository
            .findByToken(request.refreshToken())
            .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
    User user = storedToken.getUser();
    if (storedToken.isRevoked()
        || storedToken.getExpiresAt().isBefore(java.time.Instant.now())
        || !jwtService.isValidRefreshToken(request.refreshToken(), user)) {
      throw new IllegalArgumentException("Invalid refresh token");
    }

    storedToken.setRevoked(true);
    refreshTokenRepository.save(storedToken);
    return issueTokens(user);
  }

  private AuthResponse issueTokens(UserDetails userDetails) {
    String accessToken = jwtService.generateAccessToken(userDetails);
    String refreshToken = jwtService.generateRefreshToken(userDetails);
    refreshTokenRepository.save(
        new RefreshToken((User) userDetails, refreshToken, jwtService.refreshTokenExpiresAt()));
    return new AuthResponse(accessToken, refreshToken);
  }
}
