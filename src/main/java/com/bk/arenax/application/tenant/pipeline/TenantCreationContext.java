package com.bk.arenax.application.tenant.pipeline;

import com.bk.arenax.domain.account.Account;
import com.bk.arenax.domain.tenant.Branch;
import com.bk.arenax.domain.tenant.Tenant;
import com.bk.arenax.dto.request.tenant.CreateTenantRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
public class TenantCreationContext {
  private final CreateTenantRequest request;
  private final Account currentAccount;

  @Setter private Tenant tenant;
  @Setter private Branch firstBranch;

  public TenantCreationContext(CreateTenantRequest request, Account account) {
    this.request = request;
    this.currentAccount = account;
  }
}
