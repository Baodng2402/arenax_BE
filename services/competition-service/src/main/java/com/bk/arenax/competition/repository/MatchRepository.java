package com.bk.arenax.competition.repository;

import com.bk.arenax.competition.domain.entity.Match;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchRepository extends JpaRepository<Match, UUID> {
}
