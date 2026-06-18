package com.bk.arenax.domain.match.strategy;

import com.bk.arenax.domain.match.factory.SportCreationContext;
import org.springframework.stereotype.Component;

@Component
public class DefaultSportStrategy implements SportStrategy {

  @Override
  public void validateCreate(SportCreationContext context) {

    if (context.name() == null || context.name().trim().isEmpty()) {
      throw new RuntimeException("Sport name is required");
    }
    if (context.playersPerTeam() == null || context.playersPerTeam() <= 0) {
      throw new RuntimeException("Players per team must be greater than 0");
    }
    if (context.durationMinutes() == null || context.durationMinutes() <= 0) {
      throw new RuntimeException("Duration minutes must be positive");
    }
    if (context.teamCount() == null || context.teamCount() < 2) {
      throw new RuntimeException("A sport must have at least 2 teams to compete");
    }
    if (context.minPlayersToStart() != null) {
      if (context.minPlayersToStart() <= 0) {
        throw new RuntimeException("Minimum players to start must be greater than 0");
      }
      if (context.minPlayersToStart() > context.playersPerTeam()) {
        throw new RuntimeException("Minimum players to start cannot exceed players per team limit");
      }
    }

    if (context.maxScore() != null && context.maxScore() <= 0) {
      throw new RuntimeException("Max score must be a positive number");
    }

    if (context.scoringType() != null && !isValidScoringType(context.scoringType())) {
      throw new RuntimeException("Invalid scoring type! Must be GOALS, POINTS, or SETS");
    }
  }

  private boolean isValidScoringType(String scoringType) {
    return "GOALS".equalsIgnoreCase(scoringType)
        || "POINTS".equalsIgnoreCase(scoringType)
        || "SETS".equalsIgnoreCase(scoringType);
  }
}
