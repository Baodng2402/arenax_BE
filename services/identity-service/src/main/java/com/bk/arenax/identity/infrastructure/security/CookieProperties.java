package com.bk.arenax.identity.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "arenax.security.cookie")
public record CookieProperties(boolean secure) {}
