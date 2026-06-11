package com.bk.arenax.domain.match.strategy;

import com.bk.arenax.domain.match.Match;
import com.bk.arenax.domain.match.Sport;
import com.bk.arenax.domain.match.factory.MatchCreationContext;

public interface SportMatchStrategy {
    boolean supports(String sportCode);
    void validateCreate(Sport sport, MatchCreationContext context);
    void validateResult(Match match, Integer scoreTeam1, Integer scoreTeam2);
    int getTeamCount(Sport sport);
    int getPlayersPerTeam(Sport sport);
}
