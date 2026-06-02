package com.bk.arenax.domain.matches;

import com.bk.arenax.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "matches")

public class Match extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(name = "match_type", nullable = false)
    MatchType matchType;

    @Enumerated(EnumType.STRING)
    @Column(name = "sport_type", nullable = false)
    SportType sportType;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_format", nullable = false)
    MatchFormat matchFormat;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_result")
    MatchResult matchResult;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_status", nullable = false)
    @Builder.Default
    MatchStatus matchStatus = MatchStatus.PENDING;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    List<MatchSide> sides = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "players_data", columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    Map<String, Object> playersData = new HashMap<>();

    @Column(name = "started_at")
    Instant startedAt;

    @Column(name = "ended_at")
    Instant endedAt;

    public Integer getTotalPlayers() {
        return sides.stream()
                .mapToInt(MatchSide::getPlayerCount)
                .sum();
    }

    public boolean isRankMatch() {
        return matchType == MatchType.RANK;
    }

    public boolean isOneVsOne() {
        return matchFormat == MatchFormat.ONE_VS_ONE;
    }

    public boolean isTwoVsTwo() {
        return matchFormat == MatchFormat.TWO_VS_TWO;
    }

    public boolean isCustomFormat() {
        return matchFormat == MatchFormat.CUSTOM;
    }

    public boolean isFinished() {
        return matchStatus == MatchStatus.COMPLETED
                || matchStatus == MatchStatus.CANCELED;
    }

    public boolean hasResult() {
        return matchResult != null;
    }

    public boolean hasWinner() {
        return matchResult == MatchResult.SIDE1_WIN
                || matchResult == MatchResult.SIDE2_WIN;
    }

    public boolean isDraw() {
        return matchResult == MatchResult.DRAW;
    }

    public void addSide(MatchSide side) {
        this.sides.add(side);
        side.setMatch(this);
    }

    public void start() {
        this.matchStatus = MatchStatus.ONGOING;
        this.startedAt = Instant.now();
    }

    public void completeWithSide1Win(Integer scoreSide1, Integer scoreSide2) {
        complete(
                MatchResult.SIDE1_WIN,
                scoreSide1,
                scoreSide2
        );
    }

    public void completeWithSide2Win(Integer scoreSide1, Integer scoreSide2) {
        complete(
                MatchResult.SIDE2_WIN,
                scoreSide1,
                scoreSide2
        );
    }

    public void completeDraw(Integer scoreSide1, Integer scoreSide2) {
        complete(
                MatchResult.DRAW,
                scoreSide1,
                scoreSide2
        );
    }

    public void cancel() {
        this.matchStatus = MatchStatus.CANCELED;
        this.endedAt = Instant.now();
    }

    private void complete(
            MatchResult result,
            Integer scoreSide1,
            Integer scoreSide2
    ) {
        this.matchStatus = MatchStatus.COMPLETED;
        this.matchResult = result;
        setSideScore(1, scoreSide1);
        setSideScore(2, scoreSide2);
        this.endedAt = Instant.now();
    }

    private MatchSide getSide(Integer sideNumber) {
        return sides.stream()
                .filter(side -> sideNumber.equals(side.getSideNumber()))
                .findFirst()
                .orElse(null);
    }

    private void setSideScore(Integer sideNumber, Integer score) {
        MatchSide side = getSide(sideNumber);
        if (side != null) {
            side.setScore(score);
        }
    }
}
