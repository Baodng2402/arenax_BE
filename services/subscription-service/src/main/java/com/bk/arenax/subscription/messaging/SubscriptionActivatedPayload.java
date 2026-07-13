package com.bk.arenax.subscription.messaging;

import java.util.UUID;

public record SubscriptionActivatedPayload(UUID accountId, String plan, String status) {}
