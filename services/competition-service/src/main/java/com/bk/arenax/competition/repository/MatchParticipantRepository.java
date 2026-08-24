package com.bk.arenax.competition.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bk.arenax.competition.domain.entity.MatchParticipant;

public interface MatchParticipantRepository extends JpaRepository<MatchParticipant, UUID> {

  List<MatchParticipant> findAllByMatchIdOrderByTeamNumberAscCreatedAtAsc(UUID matchId);

  Optional<MatchParticipant> findByMatchIdAndUserId(UUID matchId, UUID userId);
}
