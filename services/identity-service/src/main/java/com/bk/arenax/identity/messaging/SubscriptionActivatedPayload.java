package com.bk.arenax.identity.messaging;

import java.util.UUID;

public record SubscriptionActivatedPayload(UUID accountId, String plan, String status) {}
