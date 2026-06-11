package com.bk.arenax.adapter.rest.tenant;

import com.bk.arenax.dto.request.tenant.CreateTenantRequest;
import com.bk.arenax.dto.request.tenant.UpdateTenantRequest;
import com.bk.arenax.dto.response.ApiResponse;
import com.bk.arenax.dto.response.Tenant.TenantResponse;
import com.bk.arenax.port.service.Tenant.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class TenantProfileController {
  private final TenantService tenantService;
  @GetMapping()
  public ApiResponse<TenantResponse> getTenantProfile(){
    return ApiResponse.of(tenantService.getTenant());
  }

  @PostMapping()
  public ApiResponse<TenantResponse> createTenant(@Valid @RequestBody CreateTenantRequest request){
    return ApiResponse.of(tenantService.createTenant(request));
  }

  @PatchMapping()
  public ApiResponse <TenantResponse> updateTenant(@Valid @RequestBody UpdateTenantRequest request){
    return ApiResponse.of(tenantService.updateTenant(request));
  }
}
