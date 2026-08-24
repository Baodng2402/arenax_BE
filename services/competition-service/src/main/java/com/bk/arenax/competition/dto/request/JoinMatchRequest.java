package com.bk.arenax.competition.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record JoinMatchRequest(@NotNull UUID userId, @NotNull @Min(1) @Max(2) Integer teamNumber) {}
