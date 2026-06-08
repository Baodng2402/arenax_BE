package com.bk.arenax.dto.request.MatchModule;

import jakarta.validation.constraints.NotNull;


public record JoinMatch(
        @NotNull(message = "Player id is required")
        Long playerId
) {}
