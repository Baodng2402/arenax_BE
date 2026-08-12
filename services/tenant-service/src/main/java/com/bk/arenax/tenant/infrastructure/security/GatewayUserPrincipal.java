package com.bk.arenax.tenant.infrastructure.security;

import java.util.List;
import java.util.UUID;

public record GatewayUserPrincipal(
        UUID userId,
        UUID sessionId,
        UUID accountId,
        List<String> roles,
        List<String> permissions) {
}
