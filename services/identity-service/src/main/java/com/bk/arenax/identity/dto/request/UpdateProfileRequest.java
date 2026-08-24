package com.bk.arenax.identity.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
    @Size(max = 120) String fullName, @Size(max = 500) String avatarUrl) {}
