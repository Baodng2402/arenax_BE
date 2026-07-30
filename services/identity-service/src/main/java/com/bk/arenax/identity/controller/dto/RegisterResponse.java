package com.bk.arenax.identity.controller.dto;

import java.util.UUID;

public record RegisterResponse(UUID userId, String email, String status) {}
