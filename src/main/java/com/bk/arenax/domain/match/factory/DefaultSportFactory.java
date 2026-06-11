package com.bk.arenax.domain.match.factory;

import com.bk.arenax.domain.match.Sport;
import com.bk.arenax.domain.match.strategy.SportStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultSportFactory implements SportFactory{

    private final SportStrategy sportStrategy;

    @Override
    public Sport createSport(SportCreationContext context){

        Sport newSport = new Sport();
        newSport.setName(context.name());
        newSport.setPlayersPerTeam(context.playersPerTeam());
        newSport.generateSportCode();
        newSport.setDurationMinutes(context.durationMinutes());
        newSport.setTeamCount(context.teamCount());

        newSport.setAllowDraw(context.allowDraw() != null ? context.allowDraw() : true);
        newSport.setMinPlayersToStart(context.minPlayersToStart() != null ? context.minPlayersToStart() : context.playersPerTeam());
        newSport.setMaxScore(context.maxScore());
        newSport.setScoringType(context.scoringType() != null ? context.scoringType().toUpperCase() : "GOALS");

        sportStrategy.validateCreate(context);
        return newSport;
    }
}
