package com.bk.arenax.ranking.service;

import com.bk.arenax.ranking.domain.entity.RankingHistory;
import com.bk.arenax.ranking.dto.response.PageResponse;
import com.bk.arenax.ranking.dto.response.PlayerRankingResponse;
import com.bk.arenax.ranking.dto.response.RankingHistoryResponse;
import com.bk.arenax.ranking.repository.PlayerRankingRepository;
import com.bk.arenax.ranking.repository.RankingHistoryRepository;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class RankingQueryService {

    private final PlayerRankingRepository playerRankingRepository;
    private final RankingHistoryRepository rankingHistoryRepository;

    public RankingQueryService(PlayerRankingRepository playerRankingRepository, RankingHistoryRepository rankingHistoryRepository) {
        this.playerRankingRepository = playerRankingRepository;
        this.rankingHistoryRepository = rankingHistoryRepository;
    }

    public PlayerRankingResponse getByUserId(UUID userId) {
        return playerRankingRepository.findByUserId(userId)
                .map(ranking -> new PlayerRankingResponse(
                        ranking.getUserId(),
                        ranking.getRating(),
                        ranking.getWins(),
                        ranking.getLosses()))
                .orElseThrow(() -> new EntityNotFoundException("Ranking not found"));
    }

    public PageResponse<RankingHistoryResponse> getHistory(UUID userId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "occurredAt"));

        Page<RankingHistory> historyPage = rankingHistoryRepository.findAllByUserId(userId, pageable);
        List<RankingHistoryResponse> items = historyPage.getContent().stream()
                .map(history -> new RankingHistoryResponse(
                        history.getMatchId(),
                        history.getPreviousRating(),
                        history.getNewRating(),
                        history.getNewRating() - history.getPreviousRating(),
                        history.getResult(),
                        history.getOccurredAt()
                )).toList();

        return new PageResponse<>(
                items,
                historyPage.getNumber(),
                historyPage.getSize(),
                historyPage.getTotalElements(),
                historyPage.getTotalPages(),
                historyPage.hasNext());
    }
}
