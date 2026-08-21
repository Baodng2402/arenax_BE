package com.bk.arenax.tenant.infrastructure.security;

import com.bk.arenax.security.trustedgateway.TrustedGatewayAuthentication;
import com.bk.arenax.security.trustedgateway.TrustedGatewayPrincipal;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserResolver {

    public TrustedGatewayPrincipal resolve(Authentication authentication) {
        if (authentication instanceof TrustedGatewayAuthentication gatewayAuthentication
                && gatewayAuthentication.getPrincipal() instanceof TrustedGatewayPrincipal principal) {
            return principal;
        }
        throw new AuthenticationCredentialsNotFoundException("No authenticated user found");
    }
}
