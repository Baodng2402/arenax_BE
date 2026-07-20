package com.bk.arenax.identity.infrastructure.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

  private final JwtEncoder jwtEncoder;
  private final JwtProperties jwtProperties;

  public String issueAccessToken(UUID userId,
                                 UUID accountId,
                                 List<String> roles,
                                 List<String> permissions){
    Instant now = Instant.now();
    JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(jwtProperties.issuer())
            .audience(List.of(jwtProperties.audience()))
            .issuedAt(now)
            .expiresAt(now.plusSeconds(jwtProperties.accessTokenTtlSeconds()))
            .subject(userId.toString())
            .claim("account_id",accountId==null?null:accountId.toString())
            .claim("roles",roles)
            .claim("permissions",permissions)
            .id(UUID.randomUUID().toString())
            .build();
    JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
    return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader,claims)).getTokenValue();
  }

  public String issueRefreshToken(UUID userId){
    Instant now = Instant.now();
    JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(jwtProperties.issuer())
            .audience(List.of(jwtProperties.audience()))
            .issuedAt(now)
            .expiresAt(now.plusSeconds(jwtProperties.refreshTokenTtlSeconds()))
            .subject(userId.toString())
            .claim("type","refresh")
            .id(UUID.randomUUID().toString())
            .build();
    JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
    return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader,claims)).getTokenValue();
  }

  public long getAccessTokenTtlSeconds(){
    return jwtProperties.accessTokenTtlSeconds();
  }
  public long getRefreshTokenTtlSeconds(){
    return jwtProperties.refreshTokenTtlSeconds();
  }
}
