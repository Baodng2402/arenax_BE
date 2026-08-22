package com.bk.arenax.identity.service;

import com.bk.arenax.identity.domain.User;
import com.bk.arenax.identity.dto.response.UserProfileResponse;
import com.bk.arenax.identity.repository.UserRepository;
import com.bk.arenax.identity.service.support.UserProfileResponseAssembler;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

  private final UserRepository userRepo;
  private final UserProfileResponseAssembler profileAssembler;

  @Transactional(readOnly = true)
  public UserProfileResponse getProfile(UUID userId, UUID accountId) {
    User user = userRepo.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
    return profileAssembler.assemble(user, accountId);
  }

  @Transactional
  public UserProfileResponse updateProfile(UUID userId, String fullName, String avatarUrl, UUID accountId) {
    if (fullName != null && fullName.isBlank()) {
      throw new IllegalArgumentException("Full name must not be blank");
    }
    User user = userRepo.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
    user.updateProfile(fullName, avatarUrl);
    return profileAssembler.assemble(user, accountId);
  }
}