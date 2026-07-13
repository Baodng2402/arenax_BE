package com.bk.arenax.subscription.messaging;

import java.util.UUID;

public record PersonalAccountCreatedPayload(UUID userId, UUID accountId, String accountName) {}
