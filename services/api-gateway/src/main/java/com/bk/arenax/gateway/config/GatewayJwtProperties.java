package com.bk.arenax.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "arenax.security.jwt")
public record GatewayJwtProperties(
        String issuer,
        String audience,
        String jwkSetUri) {
}
