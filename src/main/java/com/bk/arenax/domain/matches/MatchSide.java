package com.bk.arenax.domain.matches;

import com.bk.arenax.domain.account.Account;
import com.bk.arenax.domain.common.BaseEntity;
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
@Table(name = "match_sides")
public class MatchSide extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    Match match;

    @Column(name = "side_number", nullable = false)
    Integer sideNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "representative_account_id")
    Account representativeAccount;

    @Column(name = "number_of_players")
    Integer numberOfPlayers;

    @Column(name = "score")
    Integer score;

    public int getPlayerCount() {
        return numberOfPlayers == null ? 0 : numberOfPlayers;
    }

}
