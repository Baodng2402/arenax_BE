package com.bk.arenax.tenant.dto.response;

import java.util.UUID;

public record AccountSummaryResponse(
        UUID accountId,
        String name,
        String type,
        String status,
        String role,
        boolean current) {
}
