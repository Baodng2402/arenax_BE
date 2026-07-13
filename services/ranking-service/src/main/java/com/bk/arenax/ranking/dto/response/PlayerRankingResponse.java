package com.bk.arenax.ranking.dto.response;

import java.util.UUID;

public record PlayerRankingResponse(UUID userId, int rating, int wins, int losses) {
}
