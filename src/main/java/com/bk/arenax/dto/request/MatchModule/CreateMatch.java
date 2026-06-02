package com.bk.arenax.dto.request.MatchModule;
import com.bk.arenax.domain.matches.MatchType;
import com.bk.arenax.domain.matches.MatchFormat;
import com.bk.arenax.domain.matches.SportType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public record CreateMatch(
    @NotNull(message = "Account 1 is required")
    Long account1Id,

    @NotNull(message = "Account 2 is required")
    Long account2Id,

    @NotNull(message = "Number player of account 1 is required")
    @Positive(message = "Number player of account 1 must be greater than 0")
    Integer numberPlayerOfAccount1,

    @NotNull(message = "Number player of account 2 is required")
    @Positive(message = "Number player of account 2 must be greater than 0")
    Integer numberPlayerOfAccount2,

    @NotNull(message = "Match type is required")
    MatchType matchType,

    @NotNull(message = "Sport type is required")
    SportType sportType,

    @NotNull(message = "Match format is required")
    MatchFormat matchesFormat,

    Map<String, Object> playersData,

    Instant startedAt
) {
    public Map<String, Object> playersData() {
        return playersData == null ? new HashMap<>() : playersData;
    }
}
