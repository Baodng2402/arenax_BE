package com.bk.arenax.security.trustedgateway;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class TrustedGatewayAuthentication extends AbstractAuthenticationToken {

  private final TrustedGatewayPrincipal principal;

  public TrustedGatewayAuthentication(
      TrustedGatewayPrincipal principal, Collection<? extends GrantedAuthority> authorities) {
    super(authorities);
    this.principal = principal;
    setAuthenticated(true);
  }

  @Override
  public Object getCredentials() {
    return null;
  }

  @Override
  public TrustedGatewayPrincipal getPrincipal() {
    return principal;
  }
}
