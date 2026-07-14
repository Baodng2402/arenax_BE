package com.bk.arenax.ranking.repository;

import com.bk.arenax.ranking.domain.entity.RankingHistory;

import org.springframework.data.domain.Pageable;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RankingHistoryRepository extends JpaRepository<RankingHistory, UUID> {

    boolean existsByMatchId(UUID matchId);

    Page<RankingHistory> findAllByUserId(UUID userId, Pageable pageable);
}
