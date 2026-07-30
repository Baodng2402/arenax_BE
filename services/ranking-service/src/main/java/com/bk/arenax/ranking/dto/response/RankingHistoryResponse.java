package com.bk.arenax.ranking.dto.response;

import java.time.Instant;
import java.util.UUID;

public record RankingHistoryResponse(
        UUID matchId,
        int previousRating,
        int newRating,
        int ratingChange,
        String result,
        Instant finishedAt
) {}
