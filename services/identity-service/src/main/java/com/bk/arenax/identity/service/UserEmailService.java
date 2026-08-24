package com.bk.arenax.identity.service;

import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bk.arenax.identity.domain.EmailVerificationToken;
import com.bk.arenax.identity.domain.User;
import com.bk.arenax.identity.domain.UserIdentifier;
import com.bk.arenax.identity.domain.UserIdentifierType;
import com.bk.arenax.identity.dto.response.UserEmailResponse;
import com.bk.arenax.identity.dto.response.UsernameResponse;
import com.bk.arenax.identity.repository.EmailVerificationTokenRepository;
import com.bk.arenax.identity.repository.UserIdentifierRepository;
import com.bk.arenax.identity.repository.UserRepository;
import com.bk.arenax.identity.service.support.EmailNormalizationService;
import com.bk.arenax.identity.service.support.IdentityEventPublisher;
import com.bk.arenax.identity.service.support.IdentityTokenGenerator;
import com.bk.arenax.identity.service.support.IdentityTokenHasher;
import com.bk.arenax.identity.service.support.UserEmailResponseMapper;

@Service
@RequiredArgsConstructor
public class UserEmailService {

  private static final Duration EMAIL_VERIFICATION_TTL = Duration.ofHours(24);

  private final UserRepository userRepo;
  private final UserIdentifierRepository userIdentifierRepository;
  private final EmailVerificationTokenRepository emailVerificationTokenRepository;
  private final IdentityTokenHasher tokenHasher;
  private final IdentityTokenGenerator tokenGenerator;
  private final EmailNormalizationService emailNormalizationService;
  private final UserEmailResponseMapper emailResponseMapper;
  private final IdentityEventPublisher eventPublisher;

  @Transactional(readOnly = true)
  public List<UserEmailResponse> listEmails(UUID userId) {
    userRepo.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    return emailResponseMapper.toResponses(
        userIdentifierRepository.findAllByUserIdAndTypeOrderByPrimaryDescCreatedAtAsc(
            userId, UserIdentifierType.EMAIL));
  }

  @Transactional
  public UsernameResponse updateUsername(UUID userId, String username) {
    User user = userRepo.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    String normalizedUsername = normalizeUsername(username);
    if (!normalizedUsername.equals(user.getUsername())
        && userRepo.existsByUsername(normalizedUsername)) {
      throw new IllegalArgumentException("Username already exists");
    }
    user.setUsername(normalizedUsername);
    return new UsernameResponse(normalizedUsername);
  }

  @Transactional
  public void clearUsername(UUID userId) {
    User user = userRepo.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    user.clearUsername();
  }

  @Transactional
  public UserEmailResponse addEmail(UUID userId, String email) {
    User user = userRepo.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    String normalizedEmail = emailNormalizationService.normalize(email);
    if (userIdentifierRepository.existsByTypeAndNormalizedValue(
        UserIdentifierType.EMAIL, normalizedEmail)) {
      throw new IllegalArgumentException("Email already exists");
    }

    UserIdentifier identifier =
        userIdentifierRepository.save(UserIdentifier.secondaryEmail(userId, normalizedEmail));
    issueVerification(identifier, user, Instant.now());
    return emailResponseMapper.toResponse(identifier);
  }

  @Transactional
  public void setPrimaryEmail(UUID userId, UUID emailId) {
    User user = userRepo.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    UserIdentifier target =
        userIdentifierRepository
            .findByIdAndUserIdAndType(emailId, userId, UserIdentifierType.EMAIL)
            .orElseThrow(() -> new IllegalArgumentException("Email not found"));
    if (!target.isVerified()) {
      throw new IllegalArgumentException("Email must be verified before becoming primary");
    }

    List<UserIdentifier> identifiers =
        userIdentifierRepository.findAllByUserIdAndTypeOrderByPrimaryDescCreatedAtAsc(
            userId, UserIdentifierType.EMAIL);
    identifiers.forEach(
        identifier -> {
          if (identifier.getId().equals(target.getId())) {
            identifier.makePrimary();
          } else {
            identifier.makeSecondary();
          }
        });
    user.setPrimaryEmail(target.getNormalizedValue(), target.getVerifiedAt());
  }

  @Transactional
  public void removeEmail(UUID userId, UUID emailId) {
    UserIdentifier identifier =
        userIdentifierRepository
            .findByIdAndUserIdAndType(emailId, userId, UserIdentifierType.EMAIL)
            .orElseThrow(() -> new IllegalArgumentException("Email not found"));
    if (identifier.isPrimary()) {
      throw new IllegalArgumentException("Primary email cannot be removed");
    }
    emailVerificationTokenRepository.deleteAllByUserIdentifierId(identifier.getId());
    userIdentifierRepository.delete(identifier);
  }

  public UserIdentifier requirePrimaryEmail(UUID userId) {
    return userIdentifierRepository
        .findByUserIdAndTypeAndPrimaryTrue(userId, UserIdentifierType.EMAIL)
        .orElseThrow(() -> new IllegalStateException("Primary email not found for user"));
  }

  private String normalizeUsername(String username) {
    String normalized = username == null ? null : username.trim().toLowerCase(Locale.ROOT);
    if (normalized == null || normalized.isBlank()) {
      throw new IllegalArgumentException("Username must not be blank");
    }
    if (normalized.length() < 3 || normalized.length() > 40) {
      throw new IllegalArgumentException("Username must be between 3 and 40 characters");
    }
    return normalized;
  }

  private void issueVerification(UserIdentifier identifier, User user, Instant now) {
    Instant expiresAt = now.plus(EMAIL_VERIFICATION_TTL);
    String rawVerificationToken = tokenGenerator.generate();
    emailVerificationTokenRepository.save(
        EmailVerificationToken.issue(
            user.getId(), identifier.getId(), tokenHasher.hash(rawVerificationToken), expiresAt));
    eventPublisher.publishVerificationRequested(
        user, identifier, rawVerificationToken, expiresAt, now);
  }
}
