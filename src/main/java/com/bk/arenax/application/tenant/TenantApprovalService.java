package com.bk.arenax.application.tenant;

import com.bk.arenax.adapter.repository.tenant.TenantRepository;
import com.bk.arenax.application.tenant.event.TenantApprovedEvent;
import com.bk.arenax.domain.tenant.Tenant;
import com.bk.arenax.infrastructure.exception.TenantNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TenantApprovalService {
  private final TenantRepository tenantRepository;
  private final ApplicationEventPublisher events;

  @Transactional
  public void approve(Long tenantId){
    Tenant tenant = load(tenantId);
    tenant.approve();
    events.publishEvent(new TenantApprovedEvent(tenantId));
  }

  private Tenant load(Long tenantId){
    return tenantRepository.findById(tenantId)
            .orElseThrow(()-> new TenantNotFoundException(tenantId));
  }
}
