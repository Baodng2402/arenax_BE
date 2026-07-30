package com.bk.arenax.identity.controller.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserProfileResponse(
        UUID userId,
        String email,
        String fullName,
        String status,
        String avatarUrl,
        Instant emailVerifiedAt,
        UUID accountId,
        List<String> roles,
        List<String> permissions) {
}
