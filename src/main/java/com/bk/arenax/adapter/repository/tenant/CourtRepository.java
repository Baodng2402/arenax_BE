package com.bk.arenax.adapter.repository.tenant;

import com.bk.arenax.domain.tenant.Court;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CourtRepository extends
        JpaRepository<Court,Long> , QuerydslPredicateExecutor<Court> {
}
