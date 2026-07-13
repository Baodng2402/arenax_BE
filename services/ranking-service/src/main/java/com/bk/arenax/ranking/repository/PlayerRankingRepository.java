package com.bk.arenax.ranking.repository;

import com.bk.arenax.ranking.domain.entity.PlayerRanking;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRankingRepository extends JpaRepository<PlayerRanking, UUID> {

    Optional<PlayerRanking> findByUserId(UUID userId);
}
