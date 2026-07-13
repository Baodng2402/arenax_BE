package com.bk.arenax.identity.messaging;

import java.util.Set;
import java.util.UUID;

public record AuthorizationChangedPayload(UUID userId, UUID accountId, Set<String> roles, Set<String> permissions) {}
