package com.bk.arenax.application.tenant;

import com.bk.arenax.adapter.repository.tenant.SubscriptionPlanRepository;
import com.bk.arenax.adapter.repository.tenant.TenantRepository;
import com.bk.arenax.adapter.repository.tenant.TenantSubscriptionRepository;
import com.bk.arenax.domain.subscription.SubscriptionStatus;
import com.bk.arenax.domain.tenant.TenantSubscription;
import com.bk.arenax.infrastructure.exception.TenantNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

  private final TenantRepository tenantRepository;
  private final SubscriptionPlanRepository planRepository;
  private final TenantSubscriptionRepository subscriptionRepository;

  @Transactional
  public TenantSubscription startTrial(Long tenantId){
    var tenant = tenantRepository.findById(tenantId)
            .orElseThrow(()-> new TenantNotFoundException(tenantId));
    var trialPlan = planRepository.findByCode("TRIAL")
            .orElseThrow(()->new IllegalStateException("Chưa có plan trial trong DB"));
    var sub = new TenantSubscription();
    sub.setTenant(tenant);
    sub.setPlan(trialPlan);
    sub.setStatus(SubscriptionStatus.ACTIVE);
    sub.setStartDate(LocalDate.now());
    sub.setEndDate(LocalDate.now().plusDays(trialPlan.getDurationDays()));
    sub.setPricePaid(BigDecimal.ZERO);
    return subscriptionRepository.save(sub);
  }
}
