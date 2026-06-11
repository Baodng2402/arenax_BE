package com.bk.arenax.dto.response.Tenant;

import java.util.List;
import java.util.Set;

public record TenantResponse(
    String companyName,
    String taxCode,
    List<BranchResponse> branches,
    String description,
    String logoUrl,
    String thumbnailUrl,
    String email,
    Set<String> address,
    Set<String> phone,
    Double latitude,
    Double longitude,
    String socialLink) {}
