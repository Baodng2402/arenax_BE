package com.bk.arenax.identity.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserProfileResponse(
        UUID userId,
        String username,
        String primaryEmail,
        List<UserEmailResponse> emails,
        String fullName,
        String status,
        String avatarUrl,
        Instant emailVerifiedAt,
        UUID accountId,
        List<String> roles,
        List<String> permissions) {
}
