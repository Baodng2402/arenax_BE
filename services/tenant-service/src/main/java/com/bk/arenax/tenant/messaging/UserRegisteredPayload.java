package com.bk.arenax.tenant.messaging;

import java.util.UUID;

public record UserRegisteredPayload(UUID userId, String displayName) {}
