package com.bk.arenax.ranking;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.bk.arenax.ranking.infrastructure.messaging.MatchCompletedEventConsumer;
import com.bk.arenax.ranking.repository.PlayerRankingRepository;
import com.bk.arenax.ranking.repository.RankingHistoryRepository;

@SpringBootTest
class MatchCompletedEventConsumerIntegrationTests {

  @Autowired private MatchCompletedEventConsumer consumer;

  @Autowired private PlayerRankingRepository playerRankingRepository;

  @Autowired private RankingHistoryRepository rankingHistoryRepository;

  @Test
  void consumesMatchCompletedEventAndUpdatesEloRatings() throws Exception {
    UUID winnerId = UUID.randomUUID();
    UUID loserId = UUID.randomUUID();
    UUID matchId = UUID.randomUUID();
    String envelope =
        """
                {
                  "eventId": "%s",
                  "eventType": "competition.match-completed.v1",
                  "eventVersion": 1,
                  "occurredAt": "2026-08-10T00:00:00Z",
                  "correlationId": "%s",
                  "producer": "competition-service",
                  "payload": {"matchId": "%s", "matchType": "SINGLE", "team1Score": 2, "team2Score": 1,
                              "winners": ["%s"], "losers": ["%s"], "finishedAt": "2026-08-10T00:00:00Z"}
                }
                """
            .formatted(UUID.randomUUID(), matchId, matchId, winnerId, loserId);

    consumer.onMatchCompleted(envelope);
    consumer.onMatchCompleted(envelope);

    var winner = playerRankingRepository.findByUserId(winnerId).orElseThrow();
    var loser = playerRankingRepository.findByUserId(loserId).orElseThrow();
    assertThat(winner.getWins()).isEqualTo(1);
    assertThat(loser.getLosses()).isEqualTo(1);
    assertThat(winner.getRating()).isGreaterThan(loser.getRating());
    assertThat(
            rankingHistoryRepository.findAll().stream()
                .filter(history -> history.getMatchId().equals(matchId))
                .count())
        .isEqualTo(2);
  }

  @Test
  void consumingTheSameMatchTwiceIsIdempotent() throws Exception {
    UUID winnerId = UUID.randomUUID();
    UUID loserId = UUID.randomUUID();
    UUID matchId = UUID.randomUUID();
    String envelope =
        """
                {
                  "eventId": "%s",
                  "eventType": "competition.match-completed.v1",
                  "eventVersion": 1,
                  "occurredAt": "2026-08-10T00:00:00Z",
                  "correlationId": "%s",
                  "producer": "competition-service",
                  "payload": {"matchId": "%s", "matchType": "SINGLE", "team1Score": 2, "team2Score": 1,
                              "winners": ["%s"], "losers": ["%s"], "finishedAt": "2026-08-10T00:00:00Z"}
                }
                """
            .formatted(UUID.randomUUID(), matchId, matchId, winnerId, loserId);

    consumer.onMatchCompleted(envelope);
    consumer.onMatchCompleted(envelope);

    assertThat(
            rankingHistoryRepository.findAll().stream()
                .filter(history -> history.getMatchId().equals(matchId))
                .count())
        .isEqualTo(2);
  }
}
