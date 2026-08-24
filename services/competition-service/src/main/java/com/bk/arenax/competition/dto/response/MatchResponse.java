package com.bk.arenax.competition.dto.response;

import java.util.UUID;

public record MatchResponse(
    UUID id, String status, String matchType, Integer team1Score, Integer team2Score) {}
