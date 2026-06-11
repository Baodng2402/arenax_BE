package com.bk.arenax.domain.match;

import com.bk.arenax.domain.common.BaseEntity;
import com.bk.arenax.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "match_teams")
public class Team extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    Match match;

    @Column(name = "team_number", nullable = false)
    Integer teamNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "representative_account_id")
    User captainUser;

    @Column(name = "number_of_players")
    Integer numberOfPlayers;

    @Column(name = "score")
    Integer score;

    public int getPlayerCount() {
        return numberOfPlayers == null ? 0 : numberOfPlayers;
    }

}
