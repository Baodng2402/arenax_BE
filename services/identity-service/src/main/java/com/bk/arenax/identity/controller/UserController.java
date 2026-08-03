package com.bk.arenax.identity.controller;

import com.bk.arenax.identity.controller.dto.UpdateProfileRequest;
import com.bk.arenax.identity.controller.dto.UserProfileResponse;
import com.bk.arenax.identity.infrastructure.security.GatewayUserPrincipal;
import com.bk.arenax.identity.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserProfileResponse me(@AuthenticationPrincipal GatewayUserPrincipal principal) {
        return userService.getProfile(principal.userId(), principal.accountId());
    }

    @PatchMapping("/me")
    public UserProfileResponse updateMe(@AuthenticationPrincipal GatewayUserPrincipal principal,
                                        @Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(principal.userId(), request.fullName(), request.avatarUrl(), principal.accountId());
    }
}
