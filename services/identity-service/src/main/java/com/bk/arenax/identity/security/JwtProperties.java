package com.bk.arenax.identity.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "arenax.security.jwt")
public record JwtProperties(String issuer, String audience, long accessTokenTtlSeconds, long refreshTokenTtlSeconds) {}
