package com.bk.arenax.infrastructure.converter.tenant;

import com.bk.arenax.domain.tenant.Tenant;
import com.bk.arenax.dto.response.Tenant.TenantResponse;
import com.bk.arenax.infrastructure.converter.common.CommonMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CommonMapperConfig.class)
public interface TenantMapper {
  TenantResponse toResponse(Tenant tenant);
}
