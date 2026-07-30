package com.bk.arenax.identity.messaging;

import java.util.UUID;

public record UserRegisteredPayload(UUID userId, String email, String displayName) {
}
