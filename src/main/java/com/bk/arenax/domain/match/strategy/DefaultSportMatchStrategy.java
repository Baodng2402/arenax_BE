package com.bk.arenax.domain.match.strategy;

import com.bk.arenax.domain.match.Match;
import com.bk.arenax.domain.match.Sport;
import com.bk.arenax.domain.match.factory.MatchCreationContext;
import org.springframework.stereotype.Component;

    @Component
    public class DefaultSportMatchStrategy implements SportMatchStrategy {
        @Override
        public boolean supports(String sportCode) {
            return false;
        }

        @Override
        public void validateCreate(Sport sport, MatchCreationContext context) {
            if (context.playerIds() == null || context.playerIds().isEmpty()) {
                throw new RuntimeException("Players are required to create a match");
            }

            if (!context.startedAt().isBefore(context.endedAt())) {
                throw new RuntimeException("Started time must be before ended time");
            }

            if (context.playerIds().size() != sport.getPlayersPerTeam()) {
                throw new RuntimeException(
                        "This sport requires " + sport.getPlayersPerTeam() + " players per team");
            }
        }

        @Override
        public void validateResult(Match match, Integer scoreTeam1, Integer scoreTeam2) {
            if (scoreTeam1 == null || scoreTeam2 == null) {
                throw new RuntimeException("Both team scores are required");
            }

            if (scoreTeam1 < 0 || scoreTeam2 < 0) {
                throw new RuntimeException("Scores cannot be negative");
            }
        }

        @Override
        public int getTeamCount(Sport sport) {
            return sport.getTeamCount();
        }

        @Override
        public int getPlayersPerTeam(Sport sport) {
            return sport.getPlayersPerTeam();
        }
    }

