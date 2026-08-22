package com.bk.arenax.security.trustedgateway;

import java.util.List;
import java.util.UUID;

public record TrustedGatewayPrincipal(
        UUID userId,
        UUID sessionId,
        UUID accountId,
        List<String> roles,
        List<String> permissions
) {
}