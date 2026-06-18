package com.bk.arenax.dto.request.MatchModule;

import com.bk.arenax.domain.match.MatchType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

public record CreateMatch(
    @NotNull(message = "Match type is required") MatchType matchType,
    @NotNull(message = "Sport is required") Long sportId,
    @NotNull(message = "Please input the started time") Instant startedAt,
    @NotNull(message = "Please input the ended time") Instant endedAt,
    @NotNull(message = "Quantity Player is not 0") @Size(min = 2, message = "At least 2 players")
        List<Long> playerIds) {}
