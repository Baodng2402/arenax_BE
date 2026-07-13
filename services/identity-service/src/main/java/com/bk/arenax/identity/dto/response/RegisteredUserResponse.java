package com.bk.arenax.identity.dto.response;

import com.bk.arenax.identity.domain.enums.UserStatus;
import java.util.UUID;

public record RegisteredUserResponse(UUID userId, String email, String displayName, UserStatus status) {}
