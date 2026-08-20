package com.bk.arenax.identity.service;

import com.bk.arenax.identity.domain.User;
import com.bk.arenax.identity.dto.response.UserProfileResponse;
import com.bk.arenax.identity.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

  private final UserRepository userRepo;
  private final RbacService rbacService;
  private final UserEmailService userEmailService;

  @Transactional(readOnly = true)
  public UserProfileResponse getProfile(UUID userId, UUID accountId) {
    User user = userRepo.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
    return toProfileResponse(user, accountId);
  }

  @Transactional
  public UserProfileResponse updateProfile(UUID userId, String fullName, String avatarUrl, UUID accountId) {
    if (fullName != null && fullName.isBlank()) {
      throw new IllegalArgumentException("Full name must not be blank");
    }
    User user = userRepo.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
    user.updateProfile(fullName, avatarUrl);
    return toProfileResponse(user, accountId);
  }

  private UserProfileResponse toProfileResponse(User user, UUID accountId) {
    RbacService.RbacDetails rbac = rbacService.getUserRbac(user.getId());
    return new UserProfileResponse(
            user.getId(),
            user.getUsername(),
            userEmailService.requirePrimaryEmail(user.getId()).getNormalizedValue(),
            userEmailService.listEmails(user.getId()),
            user.getFullName(),
            user.getStatus().name(),
            user.getAvatarUrl(),
            user.getEmailVerifiedAt(),
            accountId,
            rbac.roles(),
            rbac.permissions());
  }
}