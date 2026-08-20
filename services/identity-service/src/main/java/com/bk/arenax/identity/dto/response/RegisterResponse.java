package com.bk.arenax.identity.dto.response;

import java.util.UUID;

public record RegisterResponse(UUID userId, String email, String status) {}
