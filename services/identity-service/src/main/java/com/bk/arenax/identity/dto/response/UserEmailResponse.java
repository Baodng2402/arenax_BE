package com.bk.arenax.identity.dto.response;

import java.time.Instant;
import java.util.UUID;

public record UserEmailResponse(
    UUID id, String email, boolean primary, boolean verified, Instant verifiedAt) {}
