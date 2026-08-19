package com.bk.arenax.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "arenax.gateway.routes")
public record GatewayRoutesProperties(
        String identityService,
        String tenantService,
        String subscriptionService,
        String competitionService) {
}
