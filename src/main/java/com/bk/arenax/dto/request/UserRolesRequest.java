package com.bk.arenax.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record UserRolesRequest(@NotNull List<String> roleCodeNames) {}
