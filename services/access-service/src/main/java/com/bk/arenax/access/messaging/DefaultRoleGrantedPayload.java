package com.bk.arenax.access.messaging;

import java.util.UUID;

public record DefaultRoleGrantedPayload(UUID userId, UUID accountId, String roleCode) {}
