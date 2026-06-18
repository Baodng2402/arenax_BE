package com.bk.arenax.adapter.repository.tenant;

import com.bk.arenax.domain.tenant.SubscriptionPlan;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {
  Optional<SubscriptionPlan> findByCode(String trial);
}
