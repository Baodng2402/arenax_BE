package com.bk.arenax.infrastructure.exception;

import com.bk.arenax.domain.tenant.TenantStatus;
import java.util.Arrays;

public class InvalidStatusTransitionException extends TenantException {
  public InvalidStatusTransitionException(TenantStatus current, TenantStatus... allowed) {
    super(
        "INVALID_STATUS_TRANSITION",
        "Trạng thái hiện tại là "
            + current
            + ", chỉ chuyển được khi đang ở "
            + Arrays.toString(allowed));
  }
}
