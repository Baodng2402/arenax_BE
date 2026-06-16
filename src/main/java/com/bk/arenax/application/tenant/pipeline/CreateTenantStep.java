package com.bk.arenax.application.tenant.pipeline;

import com.bk.arenax.domain.tenant.Tenant;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(20)
public class CreateTenantStep implements TenantCreationStep{
  @Override
  public void execute(TenantCreationContext context){
    var req = context.getRequest();
    var tenant = new Tenant();

    tenant.setCompanyName(req.companyName());
    tenant.setTaxCode(req.taxCode());
    tenant.setEmail(req.email());
    tenant.setPhone(req.phone());
    tenant.setDescription(req.description());
    tenant.setAccount(context.getCurrentAccount());
    tenant.submit();

    context.setTenant(tenant);
  }
}
