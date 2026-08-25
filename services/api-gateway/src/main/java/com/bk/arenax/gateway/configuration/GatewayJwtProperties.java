package com.bk.arenax.gateway.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@ConfigurationProperties(prefix = "arenax.security.jwt")
public record GatewayJwtProperties(String issuer, String audience, Resource publicKeyLocation) {}
