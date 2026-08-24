package com.bk.arenax.identity.infrastructure.jwt;

import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {

  private final JwtEncoder jwtEncoder;
  private final JwtProperties jwtProperties;

  public String issueAccessToken(
      UUID userId,
      UUID sessionId,
      int tokenVersion,
      UUID accountId,
      List<String> roles,
      List<String> permissions) {
    Instant now = Instant.now();
    JwtClaimsSet.Builder claimsBuilder =
        JwtClaimsSet.builder()
            .issuer(jwtProperties.issuer())
            .audience(List.of(jwtProperties.audience()))
            .issuedAt(now)
            .expiresAt(now.plusSeconds(jwtProperties.accessTokenTtlSeconds()))
            .subject(userId.toString())
            .notBefore(now)
            .claim("sid", sessionId.toString())
            .claim("token_version", tokenVersion)
            .claim("roles", roles)
            .claim("permissions", permissions)
            .id(UUID.randomUUID().toString());
    if (accountId != null) {
      claimsBuilder.claim("account_id", accountId.toString());
    }
    JwtClaimsSet claims = claimsBuilder.build();
    JwsHeader jwsHeader =
        JwsHeader.with(SignatureAlgorithm.RS256).keyId(jwtProperties.keyId()).build();
    return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
  }

  public String issueRefreshToken(UUID userId) {
    Instant now = Instant.now();
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer(jwtProperties.issuer())
            .audience(List.of(jwtProperties.audience()))
            .issuedAt(now)
            .expiresAt(now.plusSeconds(jwtProperties.refreshTokenTtlSeconds()))
            .subject(userId.toString())
            .claim("type", "refresh")
            .id(UUID.randomUUID().toString())
            .build();
    JwsHeader jwsHeader =
        JwsHeader.with(SignatureAlgorithm.RS256).keyId(jwtProperties.keyId()).build();
    return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
  }

  public long getAccessTokenTtlSeconds() {
    return jwtProperties.accessTokenTtlSeconds();
  }

  public long getRefreshTokenTtlSeconds() {
    return jwtProperties.refreshTokenTtlSeconds();
  }
}
