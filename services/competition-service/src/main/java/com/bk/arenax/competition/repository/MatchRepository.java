package com.bk.arenax.competition.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bk.arenax.competition.domain.entity.Match;

public interface MatchRepository extends JpaRepository<Match, UUID> {}
