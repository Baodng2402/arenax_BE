package com.bk.arenax.identity.infrastructure.security;

import com.bk.arenax.security.trustedgateway.TrustedGatewayAuthentication;
import com.bk.arenax.security.trustedgateway.TrustedGatewayPrincipal;
import java.util.List;
import java.util.UUID;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserResolver {

    public TrustedGatewayPrincipal resolve(Authentication authentication) {
        if (authentication instanceof TrustedGatewayAuthentication gatewayAuthentication
                && gatewayAuthentication.getPrincipal() instanceof TrustedGatewayPrincipal principal) {
            return principal;
        }
        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            Jwt token = jwtAuthenticationToken.getToken();
            return new TrustedGatewayPrincipal(
                    UUID.fromString(token.getSubject()),
                    nullableUuid(token.getClaimAsString("sid")),
                    nullableUuid(token.getClaimAsString("account_id")),
                    claimList(token, "roles"),
                    claimList(token, "permissions"));
        }
        throw new AuthenticationCredentialsNotFoundException("No authenticated user found");
    }

    private static UUID nullableUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return UUID.fromString(value);
    }

    private static List<String> claimList(Jwt token, String claim) {
        List<String> values = token.getClaimAsStringList(claim);
        return values == null ? List.of() : values;
    }
}
