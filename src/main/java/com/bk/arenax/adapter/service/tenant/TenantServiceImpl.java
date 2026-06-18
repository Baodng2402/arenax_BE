package com.bk.arenax.adapter.service.tenant;

import com.bk.arenax.adapter.repository.tenant.TenantRepository;
import com.bk.arenax.dto.request.tenant.CreateTenantRequest;
import com.bk.arenax.dto.request.tenant.UpdateTenantRequest;
import com.bk.arenax.dto.response.Tenant.TenantResponse;
import com.bk.arenax.port.service.Tenant.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {
  private final TenantRepository tenantRepo;

  @Override
  public TenantResponse getTenant() {
    return null;
  }

  @Override
  public TenantResponse createTenant(CreateTenantRequest request) {
    return null;
  }

  @Override
  public TenantResponse updateTenant(UpdateTenantRequest request) {
    return null;
  }

  @Override
  public void deleteTenant() {}
}
