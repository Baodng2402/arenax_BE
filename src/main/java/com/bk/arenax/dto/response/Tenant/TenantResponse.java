package com.bk.arenax.dto.response.Tenant;
import com.bk.arenax.domain.tenant.TenantStatus;
import java.time.Instant;

public record TenantResponse(
    Long id,
    String companyName,
    String email,
    String phone,
    TenantStatus status,
    Instant createdAt
) {}