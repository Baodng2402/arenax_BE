package com.bk.arenax.identity.infrastructure.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@ConfigurationProperties(prefix = "arenax.security.jwt")
public record JwtProperties(
    String issuer,
    String audience,
    long accessTokenTtlSeconds,
    long refreshTokenTtlSeconds,
    String keyId,
    Resource privateKeyLocation,
    Resource publicKeyLocation) {}
