package com.bk.arenax.domain.match.factory;

import com.bk.arenax.domain.match.Sport;

public record SportCreationContext(
        String name,
        Integer durationMinutes,
        Integer teamCount,
        Integer playersPerTeam,
        Boolean allowDraw,
        Integer minPlayersToStart,
        Integer maxScore,
        String scoringType
) {
}
