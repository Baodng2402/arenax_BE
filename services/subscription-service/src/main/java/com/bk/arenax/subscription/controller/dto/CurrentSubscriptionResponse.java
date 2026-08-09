package com.bk.arenax.subscription.controller.dto;

import java.util.List;
import java.util.UUID;

public record CurrentSubscriptionResponse(
        UUID accountId,
        String plan,
        String status,
        List<String> entitlements) {
}
