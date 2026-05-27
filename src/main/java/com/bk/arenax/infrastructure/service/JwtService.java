package com.bk.arenax.infrastructure.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private static final String TOKEN_TYPE_CLAIM = "type";
  private static final String ACCESS_TOKEN_TYPE = "access";
  private static final String REFRESH_TOKEN_TYPE = "refresh";

  private final SecretKey signingKey;
  private final long accessTokenExpirationMs;
  private final long refreshTokenExpirationMs;

  public JwtService(
      @Value("${application.security.jwt.secret}") String secret,
      @Value("${application.security.jwt.expiration}") long accessTokenExpirationMs,
      @Value("${application.security.jwt.refresh-token.expiration}")
          long refreshTokenExpirationMs) {
    this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.accessTokenExpirationMs = accessTokenExpirationMs;
    this.refreshTokenExpirationMs = refreshTokenExpirationMs;
  }

  public String generateAccessToken(UserDetails userDetails) {
    return buildToken(userDetails.getUsername(), ACCESS_TOKEN_TYPE, accessTokenExpirationMs);
  }

  public String generateRefreshToken(UserDetails userDetails) {
    return buildToken(userDetails.getUsername(), REFRESH_TOKEN_TYPE, refreshTokenExpirationMs);
  }

  public Instant refreshTokenExpiresAt() {
    return Instant.now().plusMillis(refreshTokenExpirationMs);
  }

  public String extractUsername(String token) {
    return claims(token).getSubject();
  }

  public boolean isValidAccessToken(String token, UserDetails userDetails) {
    return isValidToken(token, userDetails, ACCESS_TOKEN_TYPE);
  }

  public boolean isValidRefreshToken(String token, UserDetails userDetails) {
    return isValidToken(token, userDetails, REFRESH_TOKEN_TYPE);
  }

  private String buildToken(String subject, String tokenType, long expirationMs) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(subject)
        .claims(Map.of(TOKEN_TYPE_CLAIM, tokenType))
        .id(UUID.randomUUID().toString())
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusMillis(expirationMs)))
        .signWith(signingKey)
        .compact();
  }

  private boolean isValidToken(String token, UserDetails userDetails, String tokenType) {
    Claims claims = claims(token);
    return userDetails.getUsername().equals(claims.getSubject())
        && tokenType.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))
        && claims.getExpiration().after(new Date());
  }

  private Claims claims(String token) {
    return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
  }
}
