package com.bk.arenax.subscription.messaging;

import java.util.UUID;

public record SubscriptionChangedPayload(UUID accountId, String plan, String status) {}
