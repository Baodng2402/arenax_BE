package com.bk.arenax.identity.dto.response;

public record AuthTokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserProfileResponse user) {
}
