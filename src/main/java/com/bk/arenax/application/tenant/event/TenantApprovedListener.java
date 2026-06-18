package com.bk.arenax.application.tenant.event;

import com.bk.arenax.application.tenant.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class TenantApprovedListener {
  private final SubscriptionService subscriptionService;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onApproved(TenantApprovedEvent event) {
    subscriptionService.startTrial(event.tenantId());
  }
}
