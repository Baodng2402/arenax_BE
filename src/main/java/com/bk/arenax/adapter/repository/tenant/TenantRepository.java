package com.bk.arenax.adapter.repository.tenant;

import com.bk.arenax.domain.tenant.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantRepository
    extends JpaRepository<Tenant, Long>, QuerydslPredicateExecutor<Tenant> {}
