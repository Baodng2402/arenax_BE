package com.bk.arenax.ranking.repository;

import com.bk.arenax.ranking.domain.entity.RankingHistory;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RankingHistoryRepository extends JpaRepository<RankingHistory, UUID> {

    boolean existsByMatchId(UUID matchId);
}
