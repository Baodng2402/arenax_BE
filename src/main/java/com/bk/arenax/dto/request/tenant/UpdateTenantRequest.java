package com.bk.arenax.dto.request.tenant;

public record UpdateTenantRequest(
    String companyName,
    String taxCode,
    String description,
    String logoUrl,
    String thumbnailUrl,
    String email,
    String address,
    String phone,
    Double latitude,
    Double longitude,
    String socialLink) {}
