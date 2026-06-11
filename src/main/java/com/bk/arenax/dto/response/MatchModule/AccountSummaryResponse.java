package com.bk.arenax.dto.response.MatchModule;

public record AccountSummaryResponse(
        Long id,
        String name,
        String type,
        UserSummaryResponse owner
) {
    public record UserSummaryResponse(
            Long id,
            String displayName,
            String avatarUrl
    ) {}
}
