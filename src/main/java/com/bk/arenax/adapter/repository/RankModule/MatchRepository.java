package com.bk.arenax.adapter.repository.RankModule;

import com.bk.arenax.domain.match.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface MatchRepository extends JpaRepository<Match, Long>, QuerydslPredicateExecutor<Match> {
}
