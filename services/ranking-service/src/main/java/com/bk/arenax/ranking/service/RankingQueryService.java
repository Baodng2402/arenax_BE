package com.bk.arenax.ranking.service;

import jakarta.persistence.EntityNotFoundException;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.bk.arenax.ranking.dto.response.PlayerRankingResponse;
import com.bk.arenax.ranking.repository.PlayerRankingRepository;

@Service
public class RankingQueryService {

  private final PlayerRankingRepository playerRankingRepository;

  public RankingQueryService(PlayerRankingRepository playerRankingRepository) {
    this.playerRankingRepository = playerRankingRepository;
  }

  public PlayerRankingResponse getByUserId(UUID userId) {
    return playerRankingRepository
        .findByUserId(userId)
        .map(
            ranking ->
                new PlayerRankingResponse(
                    ranking.getUserId(),
                    ranking.getRating(),
                    ranking.getWins(),
                    ranking.getLosses()))
        .orElseThrow(() -> new EntityNotFoundException("Ranking not found"));
  }
}
