package com.bk.arenax.port.repository.RankModule;

import com.bk.arenax.domain.ranking.ProfileRanking;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;


@Repository
public interface ProfileRankingRepository extends JpaRepository<ProfileRanking, Long> , QuerydslPredicateExecutor<ProfileRanking> {
  Optional<ProfileRanking> findByUserId(Long userId);
}
