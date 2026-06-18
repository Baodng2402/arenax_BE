package com.bk.arenax.domain.match.factory;

import com.bk.arenax.domain.match.MatchType;
import com.bk.arenax.domain.user.User;
import java.time.Instant;
import java.util.List;

public record MatchCreationContext(
    MatchType matchType,
    Instant startedAt,
    Instant endedAt,
    List<Long> playerIds,
    User captainUser) {}
