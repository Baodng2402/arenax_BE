package com.bk.arenax.identity.controller;

import com.bk.arenax.identity.dto.request.AddEmailRequest;
import com.bk.arenax.identity.dto.request.UpdateUsernameRequest;
import com.bk.arenax.identity.dto.response.UserEmailResponse;
import com.bk.arenax.identity.dto.request.UpdateProfileRequest;
import com.bk.arenax.identity.dto.response.UserProfileResponse;
import com.bk.arenax.identity.dto.response.UsernameResponse;
import com.bk.arenax.identity.infrastructure.security.CurrentUserResolver;
import com.bk.arenax.identity.infrastructure.security.GatewayUserPrincipal;
import com.bk.arenax.identity.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CurrentUserResolver currentUserResolver;

    @GetMapping("/me")
    public UserProfileResponse me(Authentication authentication) {
        GatewayUserPrincipal principal = currentUserResolver.resolve(authentication);
        return userService.getProfile(principal.userId(), principal.accountId());
    }

    @PatchMapping("/me")
    public UserProfileResponse updateMe(Authentication authentication,
                                        @Valid @RequestBody UpdateProfileRequest request) {
        GatewayUserPrincipal principal = currentUserResolver.resolve(authentication);
        return userService.updateProfile(principal.userId(), request.fullName(), request.avatarUrl(), principal.accountId());
    }

    @PutMapping("/me/username")
    public UsernameResponse updateUsername(Authentication authentication,
                                           @Valid @RequestBody UpdateUsernameRequest request) {
        GatewayUserPrincipal principal = currentUserResolver.resolve(authentication);
        return userService.updateUsername(principal.userId(), request.username());
    }

    @DeleteMapping("/me/username")
    public ResponseEntity<Void> deleteUsername(Authentication authentication) {
        GatewayUserPrincipal principal = currentUserResolver.resolve(authentication);
        userService.clearUsername(principal.userId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me/emails")
    public List<UserEmailResponse> myEmails(Authentication authentication) {
        GatewayUserPrincipal principal = currentUserResolver.resolve(authentication);
        return userService.listEmails(principal.userId());
    }

    @PostMapping("/me/emails")
    public ResponseEntity<UserEmailResponse> addEmail(Authentication authentication,
                                                      @Valid @RequestBody AddEmailRequest request) {
        GatewayUserPrincipal principal = currentUserResolver.resolve(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.addEmail(principal.userId(), request.email()));
    }

    @PatchMapping("/me/emails/{emailId}/primary")
    public UserProfileResponse setPrimaryEmail(Authentication authentication,
                                               @PathVariable UUID emailId) {
        GatewayUserPrincipal principal = currentUserResolver.resolve(authentication);
        return userService.setPrimaryEmail(principal.userId(), emailId, principal.accountId());
    }

    @DeleteMapping("/me/emails/{emailId}")
    public ResponseEntity<Void> deleteEmail(Authentication authentication,
                                            @PathVariable UUID emailId) {
        GatewayUserPrincipal principal = currentUserResolver.resolve(authentication);
        userService.removeEmail(principal.userId(), emailId);
        return ResponseEntity.noContent().build();
    }
}
