package com.bk.arenax.tenant.infrastructure.security;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserResolver {

    public GatewayUserPrincipal resolve(Authentication authentication) {
        if (authentication instanceof GatewayTrustedAuthentication gatewayAuthentication
                && gatewayAuthentication.getPrincipal() instanceof GatewayUserPrincipal principal) {
            return principal;
        }
        throw new AuthenticationCredentialsNotFoundException("No authenticated user found");
    }
}
