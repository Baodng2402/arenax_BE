package com.bk.arenax.adapter.repository.tenant;

import com.bk.arenax.domain.tenant.BranchDayOverride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface BranchDayOverrideRepository extends
        JpaRepository<BranchDayOverride,Long>, QuerydslPredicateExecutor<BranchDayOverride> {
}
