package com.bk.arenax.ranking.service;

import com.bk.arenax.ranking.domain.entity.PlayerRanking;
import com.bk.arenax.ranking.domain.entity.RankingHistory;
import com.bk.arenax.messaging.EventEnvelope;
import com.bk.arenax.ranking.messaging.MatchCompletedPayload;
import com.bk.arenax.ranking.repository.PlayerRankingRepository;
import com.bk.arenax.ranking.repository.RankingHistoryRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MatchCompletedHandler {

    private static final int INITIAL_RATING = 1000;
    private static final int K_FACTOR = 32;

    private final PlayerRankingRepository playerRankingRepository;
    private final RankingHistoryRepository rankingHistoryRepository;

    public MatchCompletedHandler(
            PlayerRankingRepository playerRankingRepository,
            RankingHistoryRepository rankingHistoryRepository) {
        this.playerRankingRepository = playerRankingRepository;
        this.rankingHistoryRepository = rankingHistoryRepository;
    }

    @Transactional
    public void handle(EventEnvelope<MatchCompletedPayload> event) {
        if (rankingHistoryRepository.existsByMatchId(event.payload().matchId())) {
            return;
        }

        UUID winnerId = event.payload().winners().getFirst();
        UUID loserId = event.payload().losers().getFirst();

        PlayerRanking winner = playerRankingRepository.findByUserId(winnerId).orElseGet(() -> createRanking(winnerId));
        PlayerRanking loser = playerRankingRepository.findByUserId(loserId).orElseGet(() -> createRanking(loserId));

        int winnerBefore = winner.getRating();
        int loserBefore = loser.getRating();

        int winnerAfter = calculateUpdatedRating(winnerBefore, loserBefore, 1.0);
        int loserAfter = calculateUpdatedRating(loserBefore, winnerBefore, 0.0);

        winner.setRating(winnerAfter);
        winner.setWins(winner.getWins() + 1);
        loser.setRating(loserAfter);
        loser.setLosses(loser.getLosses() + 1);

        playerRankingRepository.save(winner);
        playerRankingRepository.save(loser);

        rankingHistoryRepository.save(history(event.payload().matchId(), winnerId, winnerBefore, winnerAfter, "WIN"));
        rankingHistoryRepository.save(history(event.payload().matchId(), loserId, loserBefore, loserAfter, "LOSS"));
    }

    private PlayerRanking createRanking(UUID userId) {
        PlayerRanking ranking = new PlayerRanking();
        ranking.setUserId(userId);
        ranking.setRating(INITIAL_RATING);
        ranking.setWins(0);
        ranking.setLosses(0);
        return ranking;
    }

    private RankingHistory history(UUID matchId, UUID userId, int previousRating, int newRating, String result) {
        RankingHistory history = new RankingHistory();
        history.setMatchId(matchId);
        history.setUserId(userId);
        history.setPreviousRating(previousRating);
        history.setNewRating(newRating);
        history.setResult(result);
        return history;
    }

    private int calculateUpdatedRating(int playerRating, int opponentRating, double score) {
        double expectedScore = 1.0 / (1.0 + Math.pow(10.0, (opponentRating - playerRating) / 400.0));
        return (int) Math.round(playerRating + K_FACTOR * (score - expectedScore));
    }
}
