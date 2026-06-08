package com.bk.arenax.dto.response.MatchModule;
import com.bk.arenax.domain.matches.*;

import java.time.Instant;

public record MatchResponse(
        Long id,
        MatchType matchType,
        SportType sportType,
        MatchFormat matchFormat,
        MatchResult matchResult,
        MatchStatus matchStatus,
        Instant startedAt,
        Instant endedAt,
        Instant arrivalTime,
        Instant estimatedPlayingTime
) {
}
