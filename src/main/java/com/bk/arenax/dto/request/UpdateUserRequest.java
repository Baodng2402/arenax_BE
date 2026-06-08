package com.bk.arenax.dto.request;

import com.bk.arenax.domain.user.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRequest(
    @NotBlank(message = "Name is required") String name,
    String fullName,
    String displayName,
    String phoneNumber,
    String avatarUrl,
    @NotBlank(message = "Email is required") @Email(message = "Email must be valid") String email,
    @NotNull(message = "Gender is required") Gender gender) {}
