package com.bk.arenax.domain.match;

import com.bk.arenax.domain.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "sports")

public class Sport extends BaseEntity {

    @NotNull(message = "Sport name is required")
    @Column(name = "sport_name", nullable = false)
    String name;

    @Column(name = "sport_code", unique = true, nullable = false)
    String sportCode;

    @Column(name = "duration_minutes", nullable = false)
    Integer durationMinutes;

    @Column(name = "team_count", nullable = false)
    Integer teamCount;

    @Column(name = "players_per_team", nullable = false)
    Integer playersPerTeam;

    @Column(name = "allowDraw")
    Boolean allowDraw;

    @Column(name = "min_players_to_start")
    Integer minPlayersToStart;

    @Column(name = "max_score")
    Integer maxScore;

    @Column(name = "scoring_type")
    String scoringType;

    public int getMaxPlayers() {
        return teamCount * playersPerTeam;
    }

    public void generateSportCode() {
        if (name != null) {
            this.sportCode = name
                    .trim()
                    .replaceAll("\\s+","_")
                    .toUpperCase() + playersPerTeam;
        }
    }
}
