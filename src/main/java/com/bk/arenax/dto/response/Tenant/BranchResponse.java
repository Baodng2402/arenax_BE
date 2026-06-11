package com.bk.arenax.dto.response.Tenant;

import java.util.List;

public record BranchResponse(
    Long tenantId,
    Double latitude,
    Double Longitude,
    String avatarUrl,
    String thumbnailUrl,
    String phone,
    String address,
    String socialLink,
    List<CourtResponse> courts) {}
