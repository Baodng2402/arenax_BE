package com.bk.arenax.dto.request.tenant;

public record CreateTenantRequest(
        String companyName,
        String taxCode,
        String description,
        String logoUrl,
        String thumbnailUrl,
        String email,
        String address,
        Double latitude,
        Double longitude,
        String socialLink,
        String phone
) {
}
