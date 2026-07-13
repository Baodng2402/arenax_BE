package com.bk.arenax.identity.dto.response;

public record TokenResponse(String accessToken, String refreshToken, long expiresInSeconds) {}
