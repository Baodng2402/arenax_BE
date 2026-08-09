package com.bk.arenax.subscription.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class TrustedGatewayAuthenticationFilter extends OncePerRequestFilter {

    private static final String USER_ID_HEADER = "X-Arenax-User-Id";
    private static final String SESSION_ID_HEADER = "X-Arenax-Session-Id";
    private static final String ACCOUNT_ID_HEADER = "X-Arenax-Account-Id";
    private static final String ROLES_HEADER = "X-Arenax-Roles";
    private static final String PERMISSIONS_HEADER = "X-Arenax-Permissions";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String rawUserId = request.getHeader(USER_ID_HEADER);
        if (rawUserId == null || rawUserId.isBlank()) {
            throw new InsufficientAuthenticationException("Trusted gateway headers are required");
        }

        GatewayUserPrincipal principal = new GatewayUserPrincipal(
                UUID.fromString(rawUserId),
                parseUuid(request.getHeader(SESSION_ID_HEADER)),
                parseUuid(request.getHeader(ACCOUNT_ID_HEADER)),
                parseList(request.getHeader(ROLES_HEADER)),
                parseList(request.getHeader(PERMISSIONS_HEADER)));

        List<SimpleGrantedAuthority> authorities = principal.roles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
        SecurityContextHolder.getContext().setAuthentication(new GatewayTrustedAuthentication(principal, authorities));
        filterChain.doFilter(request, response);
    }

    private UUID parseUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return UUID.fromString(value);
    }

    private List<String> parseList(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(part -> !part.isEmpty())
                .toList();
    }
}
