package com.bk.arenax.gateway.config;

import java.util.List;
import java.util.function.Function;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.function.ServerRequest;

@Component
public class GatewayHeaderFilter {

    private static final List<String> TRUSTED_IDENTITY_HEADERS = List.of(
            "X-Arenax-User-Id",
            "X-Arenax-Session-Id",
            "X-Arenax-Account-Id",
            "X-Arenax-Roles",
            "X-Arenax-Permissions");

    public Function<ServerRequest, ServerRequest> publicRouteHeaders() {
        return request -> sanitize(request, false);
    }

    public Function<ServerRequest, ServerRequest> protectedRouteHeaders() {
        return request -> sanitize(request, true);
    }

    private ServerRequest sanitize(ServerRequest request, boolean authenticatedRoute) {
        ServerRequest.Builder builder = ServerRequest.from(request);
        builder.headers(headers -> {
            TRUSTED_IDENTITY_HEADERS.forEach(headers::remove);
            if (authenticatedRoute) {
                headers.remove(HttpHeaders.AUTHORIZATION);
            }
            headers.set(RequestIdFilter.REQUEST_ID_HEADER, resolveRequestId(request));
        });

        if (authenticatedRoute) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
                builder.headers(headers -> {
                    headers.set("X-Arenax-User-Id", jwtAuthenticationToken.getToken().getSubject());
                    headers.set("X-Arenax-Session-Id", jwtAuthenticationToken.getToken().getClaimAsString("sid"));

                    String accountId = jwtAuthenticationToken.getToken().getClaimAsString("account_id");
                    if (StringUtils.hasText(accountId)) {
                        headers.set("X-Arenax-Account-Id", accountId);
                    } else {
                        headers.remove("X-Arenax-Account-Id");
                    }

                    headers.set("X-Arenax-Roles", String.join(",", jwtAuthenticationToken.getToken().getClaimAsStringList("roles")));
                    headers.set(
                            "X-Arenax-Permissions",
                            String.join(",", jwtAuthenticationToken.getToken().getClaimAsStringList("permissions")));
                });
            }
        }

        return builder.build();
    }

    private String resolveRequestId(ServerRequest request) {
        Object requestId = request.servletRequest().getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);
        if (requestId instanceof String value && StringUtils.hasText(value)) {
            return value;
        }

        String headerValue = request.headers().firstHeader(RequestIdFilter.REQUEST_ID_HEADER);
        if (StringUtils.hasText(headerValue)) {
            return headerValue;
        }

        throw new IllegalStateException("Request id must be initialized before routing");
    }
}
