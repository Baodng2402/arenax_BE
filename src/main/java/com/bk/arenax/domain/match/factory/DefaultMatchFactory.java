package com.bk.arenax.domain.match.factory;

import com.bk.arenax.domain.match.Match;
import com.bk.arenax.domain.match.Sport;
import com.bk.arenax.domain.match.Team;
import com.bk.arenax.domain.match.strategy.SportMatchStrategy;
import org.springframework.stereotype.Component;

@Component
public class DefaultMatchFactory implements MatchFactory {

    @Override
    public Match createMatch(Sport sport, MatchCreationContext matchCreationContext, SportMatchStrategy sportMatchStrategy){
        sportMatchStrategy.validateCreate(sport, matchCreationContext);

        Team team = new Team();
        team.setTeamNumber(1);
        team.setCaptainUser(matchCreationContext.captainUser());
        team.setNumberOfPlayers(matchCreationContext.playerIds().size());

        Match match = new Match();
        match.setSport(sport);
        match.setMatchType(matchCreationContext.matchType());
        match.setStartedAt(matchCreationContext.startedAt());
        match.setEndedAt(matchCreationContext.endedAt());
        match.addTeam(team);

        return match;
    }
}
