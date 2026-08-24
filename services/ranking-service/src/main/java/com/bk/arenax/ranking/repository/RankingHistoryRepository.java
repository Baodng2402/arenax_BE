package com.bk.arenax.ranking.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bk.arenax.ranking.domain.entity.RankingHistory;

public interface RankingHistoryRepository extends JpaRepository<RankingHistory, UUID> {

  boolean existsByMatchId(UUID matchId);
}
