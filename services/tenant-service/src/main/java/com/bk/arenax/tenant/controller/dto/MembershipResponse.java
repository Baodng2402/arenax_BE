package com.bk.arenax.tenant.controller.dto;

import java.util.UUID;

public record MembershipResponse(UUID membershipId, UUID userId, String role) {
}
