package com.bk.arenax.dto.response.Tenant;

import com.bk.arenax.domain.tenant.CourtStatus;
import com.bk.arenax.domain.tenant.CourtType;
import java.math.BigDecimal;

public record CourtResponse(
    Long branchId,
    Long name,
    CourtType type,
    CourtStatus status,
    BigDecimal basePrice,
    String description) {}
