package com.bk.arenax.application.tenant.pipeline;

import com.bk.arenax.domain.tenant.Branch;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(30)
public class CreateFirstBranchStep implements TenantCreationStep{
  @Override
  public void execute(TenantCreationContext context){
    var branchReq = context.getRequest().firstBranch();
    var branch = new Branch();
    branch.setName(branchReq.name());
    branch.setAddress(branchReq.address());
    branch.setLatitude(branchReq.latitude());
    branch.setLongitude(branchReq.longitude());
    branch.setPhone(branchReq.phone());
    context.getTenant().addBranch(branch);
    context.setFirstBranch(branch);
  }
}
