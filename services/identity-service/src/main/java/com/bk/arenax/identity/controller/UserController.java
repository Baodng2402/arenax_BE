package com.bk.arenax.identity.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

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

import com.bk.arenax.identity.dto.request.AddEmailRequest;
import com.bk.arenax.identity.dto.request.UpdateProfileRequest;
import com.bk.arenax.identity.dto.request.UpdateUsernameRequest;
import com.bk.arenax.identity.dto.response.UserEmailResponse;
import com.bk.arenax.identity.dto.response.UserProfileResponse;
import com.bk.arenax.identity.dto.response.UsernameResponse;
import com.bk.arenax.identity.infrastructure.security.CurrentUserResolver;
import com.bk.arenax.identity.service.ProfileService;
import com.bk.arenax.identity.service.UserEmailService;
import com.bk.arenax.security.trustedgateway.TrustedGatewayPrincipal;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

  private final ProfileService profileService;
  private final UserEmailService userEmailService;
  private final CurrentUserResolver currentUserResolver;

  @GetMapping("/me")
  public UserProfileResponse me(Authentication authentication) {
    TrustedGatewayPrincipal principal = currentUserResolver.resolve(authentication);
    return profileService.getProfile(principal.userId(), principal.accountId());
  }

  @PatchMapping("/me")
  public UserProfileResponse updateMe(
      Authentication authentication, @Valid @RequestBody UpdateProfileRequest request) {
    TrustedGatewayPrincipal principal = currentUserResolver.resolve(authentication);
    return profileService.updateProfile(
        principal.userId(), request.fullName(), request.avatarUrl(), principal.accountId());
  }

  @PutMapping("/me/username")
  public UsernameResponse updateUsername(
      Authentication authentication, @Valid @RequestBody UpdateUsernameRequest request) {
    TrustedGatewayPrincipal principal = currentUserResolver.resolve(authentication);
    return userEmailService.updateUsername(principal.userId(), request.username());
  }

  @DeleteMapping("/me/username")
  public ResponseEntity<Void> deleteUsername(Authentication authentication) {
    TrustedGatewayPrincipal principal = currentUserResolver.resolve(authentication);
    userEmailService.clearUsername(principal.userId());
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/me/emails")
  public List<UserEmailResponse> myEmails(Authentication authentication) {
    TrustedGatewayPrincipal principal = currentUserResolver.resolve(authentication);
    return userEmailService.listEmails(principal.userId());
  }

  @PostMapping("/me/emails")
  public ResponseEntity<UserEmailResponse> addEmail(
      Authentication authentication, @Valid @RequestBody AddEmailRequest request) {
    TrustedGatewayPrincipal principal = currentUserResolver.resolve(authentication);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(userEmailService.addEmail(principal.userId(), request.email()));
  }

  @PatchMapping("/me/emails/{emailId}/primary")
  public UserProfileResponse setPrimaryEmail(
      Authentication authentication, @PathVariable UUID emailId) {
    TrustedGatewayPrincipal principal = currentUserResolver.resolve(authentication);
    userEmailService.setPrimaryEmail(principal.userId(), emailId);
    return profileService.getProfile(principal.userId(), principal.accountId());
  }

  @DeleteMapping("/me/emails/{emailId}")
  public ResponseEntity<Void> deleteEmail(
      Authentication authentication, @PathVariable UUID emailId) {
    TrustedGatewayPrincipal principal = currentUserResolver.resolve(authentication);
    userEmailService.removeEmail(principal.userId(), emailId);
    return ResponseEntity.noContent().build();
  }
}
