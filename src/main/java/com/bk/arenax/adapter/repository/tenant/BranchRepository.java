package com.bk.arenax.adapter.repository.tenant;

import com.bk.arenax.domain.tenant.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface BranchRepository
    extends JpaRepository<Branch, Long>, QuerydslPredicateExecutor<Branch> {}
