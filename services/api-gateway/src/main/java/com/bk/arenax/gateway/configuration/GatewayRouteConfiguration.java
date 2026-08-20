package com.bk.arenax.gateway.configuration;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.path;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class GatewayRouteConfiguration {

    @Bean
    RouterFunction<ServerResponse> gatewayRoutes(
            GatewayRoutesProperties routesProperties,
            GatewayHeaderFilter gatewayHeaderFilter) {
        return route("identity-auth-route")
                .route(path("/api/v1/auth/**"), http())
                .before(uri(routesProperties.identityService()))
                .before(gatewayHeaderFilter.publicRouteHeaders())
                .build()
                .and(route("identity-users-route")
                        .route(path("/api/v1/users/**"), http())
                        .before(uri(routesProperties.identityService()))
                        .before(gatewayHeaderFilter.protectedRouteHeaders())
                        .build())
                .and(route("tenant-accounts-route")
                        .route(path("/api/v1/accounts/**"), http())
                        .before(uri(routesProperties.tenantService()))
                        .before(gatewayHeaderFilter.protectedRouteHeaders())
                        .build())
                .and(route("subscription-route")
                        .route(path("/api/v1/subscriptions/**"), http())
                        .before(uri(routesProperties.subscriptionService()))
                        .before(gatewayHeaderFilter.protectedRouteHeaders())
                        .build())
                .and(route("competition-sports-route")
                        .route(path("/api/v1/sports/**"), http())
                        .before(uri(routesProperties.competitionService()))
                        .before(gatewayHeaderFilter.protectedRouteHeaders())
                        .build())
                .and(route("competition-matches-route")
                        .route(path("/api/v1/matches/**"), http())
                        .before(uri(routesProperties.competitionService()))
                        .before(gatewayHeaderFilter.protectedRouteHeaders())
                        .build());
    }
}