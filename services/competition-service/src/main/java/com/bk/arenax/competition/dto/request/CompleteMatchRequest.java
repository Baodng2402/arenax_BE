package com.bk.arenax.competition.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CompleteMatchRequest(
    @NotNull @Min(0) Integer team1Score, @NotNull @Min(0) Integer team2Score) {}
