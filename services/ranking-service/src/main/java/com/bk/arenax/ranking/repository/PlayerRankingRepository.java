package com.bk.arenax.ranking.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bk.arenax.ranking.domain.entity.PlayerRanking;

public interface PlayerRankingRepository extends JpaRepository<PlayerRanking, UUID> {

  Optional<PlayerRanking> findByUserId(UUID userId);
}
