package com.bk.arenax.tenant.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateWorkspaceRequest(@NotBlank @Size(max = 120) String name) {
    public CreateWorkspaceRequest {
        name = name == null ? null : name.trim();
    }
}
