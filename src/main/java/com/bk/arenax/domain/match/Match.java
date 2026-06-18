package com.bk.arenax.domain.match;

import com.bk.arenax.domain.common.BaseEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "matches")
public class Match extends BaseEntity {

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(name = "match_type", nullable = false)
  MatchType matchType = MatchType.FUN;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sport_id", nullable = false)
  Sport sport;

  @Enumerated(EnumType.STRING)
  @Column(name = "match_result")
  MatchResult matchResult;

  @Enumerated(EnumType.STRING)
  @Column(name = "match_status", nullable = false)
  @Builder.Default
  MatchStatus matchStatus = MatchStatus.PENDING;

  @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  List<Team> teams = new ArrayList<>();

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "players_data", columnDefinition = "jsonb", nullable = false)
  @Builder.Default
  Map<String, Object> playersData = new HashMap<>();

  @Column(name = "started_at")
  Instant startedAt;

  @Column(name = "ended_at")
  Instant endedAt;

  public Instant getArrivalTime() {
    return startedAt.plusSeconds(900);
  }

  public boolean isRankMatch() {
    return matchType == MatchType.RANK;
  }

  public boolean isFinished() {
    return matchStatus == MatchStatus.COMPLETED || matchStatus == MatchStatus.CANCELED;
  }

  public boolean hasResult() {
    return matchResult != null;
  }

  public boolean hasWinner() {
    return matchResult == MatchResult.TEAM1_WIN || matchResult == MatchResult.TEAM2_WIN;
  }

  public boolean isDraw() {
    return matchResult == MatchResult.DRAW;
  }

  public void addTeam(Team team) {
    this.teams.add(team);
    team.setMatch(this);
  }

  public void start() {
    this.matchStatus = MatchStatus.ONGOING;
    this.startedAt = Instant.now();
  }

  public void completeWithTeam1Win(Integer scoreTeam1, Integer scoreTeam2) {
    complete(MatchResult.TEAM1_WIN, scoreTeam1, scoreTeam2);
  }

  public void completeWithTeam2Win(Integer scoreTeam1, Integer scoreTeam2) {
    complete(MatchResult.TEAM2_WIN, scoreTeam1, scoreTeam2);
  }

  public void completeDraw(Integer scoreTeam1, Integer scoreTeam2) {
    complete(MatchResult.DRAW, scoreTeam1, scoreTeam2);
  }

  public void cancel() {
    this.matchStatus = MatchStatus.CANCELED;
    this.endedAt = Instant.now();
  }

  private void complete(MatchResult result, Integer scoreTeam1, Integer scoreTeam2) {
    this.matchStatus = MatchStatus.COMPLETED;
    this.matchResult = result;
    setTeamScore(1, scoreTeam1);
    setTeamScore(2, scoreTeam2);
    this.endedAt = Instant.now();
  }

  private Team getTeam(Integer teamNumber) {
    return teams.stream()
        .filter(team -> teamNumber.equals(team.getTeamNumber()))
        .findFirst()
        .orElse(null);
  }

  private void setTeamScore(Integer teamNumber, Integer score) {
    Team team = getTeam(teamNumber);
    if (team != null) {
      team.setScore(score);
    }
  }
}
