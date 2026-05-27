package com.bk.arenax.dto.request;

import com.bk.arenax.domain.user.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
    @NotBlank(message = "Name is required") String name,
    String fullName,
    String displayName,
    String phoneNumber,
    String avatarUrl,
    @NotBlank(message = "Email is required") @Email(message = "Email must be valid") String email,
    @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must have at least 6 characters")
        String password,
    @NotNull(message = "Gender is required") Gender gender) {}
