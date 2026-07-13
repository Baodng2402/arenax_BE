package com.bk.arenax.competition.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateMatchRequest(@NotNull UUID sportId, @NotNull String matchType, @NotNull UUID captainUserId) {
}
