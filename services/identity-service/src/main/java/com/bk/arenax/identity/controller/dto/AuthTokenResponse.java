package com.bk.arenax.identity.controller.dto;

public record AuthTokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserProfileResponse user) {
}
