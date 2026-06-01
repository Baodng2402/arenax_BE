package com.bk.arenax.domain.matches;

import com.bk.arenax.domain.account.Account;
import com.bk.arenax.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Entity(name = "matches")

public class Matches extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account1_id", nullable = false)
    Account account1;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account1_id", nullable = false)
    Account account2;

    @Column(name = "number_player_of_account1", nullable = false)
    Integer numberPlayerOfAccount1;

    @Column(name = "number_player_of_account2", nullable = false)
    Integer numberPlayerOfAccount2;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_type", nullable = false)
    MatchesType matchType;

    @Enumerated(EnumType.STRING)
    @Column(name = "sport_type", nullable = false)
    SportType sportType;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_format", nullable = false)
    MatchesFormat matchesFormat;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_result", nullable = false)
    MatchesResult matchResult;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_status", nullable = false)
    MatchesStatus matchStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_account_id")
    Account winnerAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loser_account_id")
    Account loserAccount;

    @Column(name = "score_account1")
    Integer scoreAccount1;

    @Column(name = "score_account2")
    Integer scoreAccount2;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "players_data", columnDefinition = "jsonb",nullable = false)
    Map<String, Object> playersData = new HashMap<>();

    @Column(name = "started_at")
    Instant startedAt;

    @Column(name = "ended_at")
    Instant endedAt;

    public Integer getTotalPlayers() {
        return numberPlayerOfAccount1 + numberPlayerOfAccount2;
    }
    public boolean isRankMatch() {
        if( matchType == MatchesType.RANK ) {
            return true;
        } else {
            return matchType == MatchesType.FUN;
        }
    }

    public boolean isOneVsOne() {
        if ( matchesFormat == MatchesFormat.ONE_VS_ONE ) {
            return true;
        } else if (matchesFormat == MatchesFormat.TWO_VS_TWO) {
            return true;
        }
        return matchesFormat == MatchesFormat.CUSTOM;
    }

    public boolean isFinished() {
        if(matchStatus == MatchesStatus.COMPLETED){
            return true;
        } else if (matchStatus == MatchesStatus.CANCELED) {
            return true;
        } else if (matchStatus == MatchesStatus.PENDING) {
            return true;
        }
        return matchStatus == MatchesStatus.ONGOING;
    }
    
    public boolean isAccountWin() {
        if(matchResult == MatchesResult.ACCOUNT1_WIN) {
            return true;
        } else if (matchResult == MatchesResult.ACCOUNT2_WIN) {
            return true;
        }
        return matchResult == MatchesResult.DRAW;
    }
}
