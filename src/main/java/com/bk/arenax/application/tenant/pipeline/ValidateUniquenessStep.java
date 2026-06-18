package com.bk.arenax.application.tenant.pipeline;

import com.bk.arenax.adapter.repository.tenant.TenantRepository;
import com.bk.arenax.infrastructure.exception.AlreadyHasTenantException;
import com.bk.arenax.infrastructure.exception.DuplicateTenantException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(10)
@RequiredArgsConstructor
public class ValidateUniquenessStep implements TenantCreationStep {

  private final TenantRepository tenantRepository;

  @Override
  public void execute(TenantCreationContext context) {
    var req = context.getRequest();
    if (tenantRepository.existsByCompanyNameIgnoreCase(req.companyName())) {
      throw new DuplicateTenantException(req.companyName());
    }
    if (tenantRepository.existsByAccount_Id(context.getCurrentAccount().getId())) {
      throw new AlreadyHasTenantException();
    }
  }
}
