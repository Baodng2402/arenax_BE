package com.bk.arenax.identity.messaging;

import java.time.Instant;
import java.util.UUID;

public record UserVerificationRequestedPayload(
        UUID userId,
        String email,
        String displayName,
        String verificationToken,
        Instant expiresAt) {}
