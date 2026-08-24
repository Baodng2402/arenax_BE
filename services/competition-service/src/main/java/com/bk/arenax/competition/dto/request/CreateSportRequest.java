package com.bk.arenax.competition.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateSportRequest(@NotBlank String name) {}
