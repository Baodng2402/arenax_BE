package com.bk.arenax.dto.response;

import com.bk.arenax.domain.user.Gender;
import com.bk.arenax.domain.user.User;

public record UserResponse(
        Long id,
        String name,
        String email,
        Gender gender
) {}
