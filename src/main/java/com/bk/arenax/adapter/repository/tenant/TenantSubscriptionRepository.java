package com.bk.arenax.adapter.repository.tenant;

import com.bk.arenax.domain.tenant.TenantSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantSubscriptionRepository extends JpaRepository<TenantSubscription,Long> {
}
