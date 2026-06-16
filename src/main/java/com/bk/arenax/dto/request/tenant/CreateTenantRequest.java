package com.bk.arenax.dto.request.tenant;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record CreateTenantRequest(
    @NotBlank @Size(max = 150) String companyName,
    @Size(max = 20) String taxCode,
    @Email @NotBlank String email,
    @NotBlank String phone,
    @Size(max = 1000) String description,
    @Valid @NotNull FirstBranchInfo firstBranch
) {
  public record FirstBranchInfo(
      @NotBlank @Size(max = 150) String name,
      @NotBlank @Size(max = 300) String address,
      Double latitude,
      Double longitude,
      String phone
  ) {}
}