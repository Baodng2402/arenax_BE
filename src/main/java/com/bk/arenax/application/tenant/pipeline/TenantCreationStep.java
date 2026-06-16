package com.bk.arenax.application.tenant.pipeline;

public interface TenantCreationStep {
  void execute(TenantCreationContext context);
}
