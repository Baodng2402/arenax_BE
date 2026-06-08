package com.bk.arenax.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateRoleRequest(
    @NotBlank(message = "Role name is required") String name,
    @NotBlank(message = "Role code is required") String codeName,
    String description) {}
