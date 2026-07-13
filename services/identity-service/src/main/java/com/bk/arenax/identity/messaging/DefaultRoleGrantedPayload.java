package com.bk.arenax.identity.messaging;

import java.util.UUID;

public record DefaultRoleGrantedPayload(UUID userId, UUID accountId, String roleCode) {}
