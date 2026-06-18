package com.bk.arenax.port.service.Tenant;

import com.bk.arenax.dto.request.tenant.CreateTenantRequest;
import com.bk.arenax.dto.request.tenant.UpdateTenantRequest;
import com.bk.arenax.dto.response.Tenant.TenantResponse;

public interface TenantService {
  TenantResponse getTenant();

  TenantResponse createTenant(CreateTenantRequest request);

  TenantResponse updateTenant(UpdateTenantRequest request);

  void deleteTenant();
}
