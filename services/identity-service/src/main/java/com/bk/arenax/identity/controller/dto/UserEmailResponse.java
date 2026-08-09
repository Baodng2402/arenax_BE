package com.bk.arenax.identity.controller.dto;

import java.time.Instant;
import java.util.UUID;

public record UserEmailResponse(
        UUID id,
        String email,
        boolean primary,
        boolean verified,
        Instant verifiedAt) {
}
