package com.bk.arenax.access.messaging;

import java.util.UUID;

public record PersonalAccountCreatedPayload(UUID userId, UUID accountId, String accountName) {}
