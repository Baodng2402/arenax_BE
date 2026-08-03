package com.bk.arenax.identity.infrastructure.security;

import java.util.Collection;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class GatewayTrustedAuthentication extends AbstractAuthenticationToken {

  private final GatewayUserPrincipal principal;

  public GatewayTrustedAuthentication(GatewayUserPrincipal principal, Collection<? extends GrantedAuthority> authorities) {
    super(authorities);
    this.principal = principal;
    setAuthenticated(true);
  }

  @Override
  public Object getCredentials() {
    return null;
  }

  @Override
  public Object getPrincipal() {
    return principal;
  }
}
