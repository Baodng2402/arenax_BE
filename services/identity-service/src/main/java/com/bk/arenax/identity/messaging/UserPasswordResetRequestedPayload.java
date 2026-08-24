package com.bk.arenax.identity.messaging;

import java.time.Instant;
import java.util.UUID;

public record UserPasswordResetRequestedPayload(
    UUID userId, String email, String displayName, String resetToken, Instant expiresAt) {}
