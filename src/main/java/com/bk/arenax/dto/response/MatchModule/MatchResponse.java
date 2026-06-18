package com.bk.arenax.dto.response.MatchModule;

import com.bk.arenax.domain.match.MatchResult;
import com.bk.arenax.domain.match.MatchStatus;
import com.bk.arenax.domain.match.MatchType;
import java.time.Instant;

public record MatchResponse(
    Long id,
    MatchType matchType,
    Long sportId,
    String sportCode,
    String sportName,
    MatchResult matchResult,
    MatchStatus matchStatus,
    Instant startedAt,
    Instant endedAt,
    Instant arrivalTime) {}
