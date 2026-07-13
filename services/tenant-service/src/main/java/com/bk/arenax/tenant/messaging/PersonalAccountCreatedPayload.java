package com.bk.arenax.tenant.messaging;

import java.util.UUID;

public record PersonalAccountCreatedPayload(UUID userId, UUID accountId, String accountName) {
}
