package com.bk.arenax.identity.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
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
    return StringUtils.hasText(request.getHeader(HttpHeaders.AUTHORIZATION))
            || !request.getRequestURI().startsWith("/api/");
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
          throws ServletException, IOException {
    UUID userId = nullableUuid(request.getHeader(USER_ID_HEADER));
    if (userId != null) {
      GatewayUserPrincipal principal = new GatewayUserPrincipal(
              userId,
              nullableUuid(request.getHeader(SESSION_ID_HEADER)),
              nullableUuid(request.getHeader(ACCOUNT_ID_HEADER)),
              splitCsv(request.getHeader(ROLES_HEADER)),
              splitCsv(request.getHeader(PERMISSIONS_HEADER)));

      List<SimpleGrantedAuthority> authorities = new ArrayList<>();
      principal.roles().forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
      principal.permissions().forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission)));

      SecurityContextHolder.getContext()
              .setAuthentication(new GatewayTrustedAuthentication(principal, authorities));
    }
    filterChain.doFilter(request, response);
  }

  private static UUID nullableUuid(String value) {
    if (!StringUtils.hasText(value)) {
      return null;
    }
    try {
      return UUID.fromString(value);
    } catch (IllegalArgumentException exception) {
      return null;
    }
  }

  private static List<String> splitCsv(String value) {
    if (!StringUtils.hasText(value)) {
      return List.of();
    }
    List<String> items = new ArrayList<>();
    for (String item : value.split(",")) {
      if (StringUtils.hasText(item)) {
        items.add(item.trim());
      }
    }
    return List.copyOf(items);
  }
}
