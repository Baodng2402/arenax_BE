package com.bk.arenax.gateway;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "arenax.gateway.routes")
public record GatewayRouteProperties(
        String identityService,
        String accessService,
        String tenantService,
        String subscriptionService,
        String competitionService,
        String rankingService) {}
