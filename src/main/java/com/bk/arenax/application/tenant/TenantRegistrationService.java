package com.bk.arenax.application.tenant;

import com.bk.arenax.adapter.repository.tenant.TenantRepository;
import com.bk.arenax.application.tenant.event.TenantRegisteredEvent;
import com.bk.arenax.application.tenant.pipeline.TenantCreationContext;
import com.bk.arenax.application.tenant.pipeline.TenantCreationStep;
import com.bk.arenax.domain.account.Account;
import com.bk.arenax.domain.tenant.Tenant;
import com.bk.arenax.dto.request.tenant.CreateTenantRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TenantRegistrationService {
  private final List<TenantCreationStep> steps;
  private final TenantRepository tenantRepository;
  private final ApplicationEventPublisher events;

  @Transactional
  public Tenant register(CreateTenantRequest request, Account currentAccount) {
    var ctx = new TenantCreationContext(request, currentAccount);
    for (TenantCreationStep step : steps) {
      step.execute(ctx);
    }
    var saved = tenantRepository.save(ctx.getTenant());
    events.publishEvent(new TenantRegisteredEvent(saved.getId()));
    return saved;
  }
}
