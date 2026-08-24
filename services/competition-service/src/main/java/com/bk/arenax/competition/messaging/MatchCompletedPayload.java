package com.bk.arenax.competition.messaging;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MatchCompletedPayload(
    UUID matchId,
    String matchType,
    int team1Score,
    int team2Score,
    List<UUID> winners,
    List<UUID> losers,
    Instant finishedAt) {}
