package com.bk.arenax.identity.service;

import com.bk.arenax.identity.dto.response.UserEmailResponse;
import com.bk.arenax.identity.dto.response.UserProfileResponse;
import com.bk.arenax.identity.dto.response.UsernameResponse;
import com.bk.arenax.identity.domain.EmailVerificationToken;
import com.bk.arenax.identity.domain.OutboxEvent;
import com.bk.arenax.identity.domain.PasswordResetToken;
import com.bk.arenax.identity.domain.User;
import com.bk.arenax.identity.domain.UserIdentifier;
import com.bk.arenax.identity.domain.UserIdentifierType;
import com.bk.arenax.identity.messaging.UserPasswordResetRequestedPayload;
import com.bk.arenax.identity.messaging.UserVerificationRequestedPayload;
import com.bk.arenax.identity.repository.EmailVerificationTokenRepository;
import com.bk.arenax.identity.repository.OutboxEventRepository;
import com.bk.arenax.identity.repository.PasswordResetTokenRepository;
import com.bk.arenax.identity.repository.RefreshSessionRepository;
import com.bk.arenax.identity.repository.UserIdentifierRepository;
import com.bk.arenax.identity.repository.UserRepository;
import com.bk.arenax.identity.service.support.EmailNormalizationService;
import com.bk.arenax.identity.service.support.IdentityEventSerializer;
import com.bk.arenax.identity.service.support.IdentityTokenGenerator;
import com.bk.arenax.identity.service.support.IdentityTokenHasher;
import com.bk.arenax.messaging.EventEnvelope;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
public class UserService {
  private static final Duration EMAIL_VERIFICATION_TTL = Duration.ofHours(24);
  private static final Duration PASSWORD_RESET_TTL = Duration.ofHours(1);

  private final UserRepository userRepo;
  private final UserIdentifierRepository userIdentifierRepository;
  private final EmailVerificationTokenRepository emailVerificationTokenRepository;
  private final OutboxEventRepository outboxEventRepository;
  private final RefreshSessionRepository refreshSessionRepository;
  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final com.bk.arenax.identity.infrastructure.jwt.JwtService jwtService;
  private final RbacService rbacService;
  private final IdentityTokenHasher tokenHasher;
  private final IdentityTokenGenerator tokenGenerator;
  private final IdentityEventSerializer eventSerializer;
  private final EmailNormalizationService emailNormalizationService;

  @Transactional
  public void requestPasswordReset(String email) {
    findVerifiedEmailIdentifier(emailNormalizationService.normalize(email)).ifPresent(identifier -> {
      User user = userRepo.findById(identifier.getUserId())
              .orElseThrow(() -> new IllegalStateException("User not found for password reset identifier"));
      Instant now = Instant.now();
      Instant expiresAt = now.plus(PASSWORD_RESET_TTL);
      String rawResetToken = tokenGenerator.generate();
      passwordResetTokenRepository.save(
              PasswordResetToken.issue(user.getId(), tokenHasher.hash(rawResetToken), expiresAt));
      outboxEventRepository.save(OutboxEvent.create(
              "identity.user.password-reset-requested.v1",
              1,
              user.getId(),
              "identity-service",
              now,
              eventSerializer.writePayload(new EventEnvelope<>(
                      UUID.randomUUID(),
                      "identity.user.password-reset-requested.v1",
                      1,
                      now,
                      user.getId(),
                      "identity-service",
                      new UserPasswordResetRequestedPayload(
                              user.getId(),
                              identifier.getNormalizedValue(),
                              user.getFullName(),
                              rawResetToken,
                              expiresAt)))));
    });
  }

  @Transactional
  public void resetPassword(String rawToken, String newPassword) {
    Instant now = Instant.now();
    PasswordResetToken token = passwordResetTokenRepository.findByTokenHash(tokenHasher.hash(rawToken))
            .orElseThrow(() -> new IllegalArgumentException("Invalid password reset token"));
    if (!token.isAvailableAt(now)) {
      throw new IllegalStateException("Password reset token is no longer valid");
    }

    User user = userRepo.findById(token.getUserId())
            .orElseThrow(() -> new IllegalStateException("User not found for password reset token"));
    token.consume(now);
    user.changePasswordHash(passwordEncoder.encode(newPassword), now);
    refreshSessionRepository.findAllByUserId(user.getId()).forEach(session -> session.revoke(now));
  }

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

  @Transactional(readOnly = true)
  public List<UserEmailResponse> listEmails(UUID userId) {
    userRepo.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    return emailResponses(userId);
  }

  @Transactional
  public UsernameResponse updateUsername(UUID userId, String username) {
    User user = userRepo.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
    String normalizedUsername = normalizeUsername(username);
    if (!normalizedUsername.equals(user.getUsername()) && userRepo.existsByUsername(normalizedUsername)) {
      throw new IllegalArgumentException("Username already exists");
    }
    user.setUsername(normalizedUsername);
    return new UsernameResponse(normalizedUsername);
  }

  @Transactional
  public void clearUsername(UUID userId) {
    User user = userRepo.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
    user.clearUsername();
  }

  @Transactional
  public UserEmailResponse addEmail(UUID userId, String email) {
    User user = userRepo.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
    String normalizedEmail = emailNormalizationService.normalize(email);
    if (userIdentifierRepository.existsByTypeAndNormalizedValue(UserIdentifierType.EMAIL, normalizedEmail)) {
      throw new IllegalArgumentException("Email already exists");
    }

    UserIdentifier identifier = userIdentifierRepository.save(
            UserIdentifier.secondaryEmail(userId, normalizedEmail));
    issueVerification(identifier, user, Instant.now());
    return toEmailResponse(identifier);
  }

  @Transactional
  public UserProfileResponse setPrimaryEmail(UUID userId, UUID emailId, UUID accountId) {
    User user = userRepo.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
    UserIdentifier target = userIdentifierRepository.findByIdAndUserIdAndType(emailId, userId, UserIdentifierType.EMAIL)
            .orElseThrow(() -> new IllegalArgumentException("Email not found"));
    if (!target.isVerified()) {
      throw new IllegalArgumentException("Email must be verified before becoming primary");
    }

    List<UserIdentifier> identifiers = userIdentifierRepository
            .findAllByUserIdAndTypeOrderByPrimaryDescCreatedAtAsc(userId, UserIdentifierType.EMAIL);
    identifiers.forEach(identifier -> {
      if (identifier.getId().equals(target.getId())) {
        identifier.makePrimary();
      } else {
        identifier.makeSecondary();
      }
    });
    user.setPrimaryEmail(target.getNormalizedValue(), target.getVerifiedAt());
    return toProfileResponse(user, accountId);
  }

  @Transactional
  public void removeEmail(UUID userId, UUID emailId) {
    UserIdentifier identifier = userIdentifierRepository.findByIdAndUserIdAndType(emailId, userId, UserIdentifierType.EMAIL)
            .orElseThrow(() -> new IllegalArgumentException("Email not found"));
    if (identifier.isPrimary()) {
      throw new IllegalArgumentException("Primary email cannot be removed");
    }
    emailVerificationTokenRepository.deleteAllByUserIdentifierId(identifier.getId());
    userIdentifierRepository.delete(identifier);
  }

  private UserProfileResponse toProfileResponse(User user, UUID accountId) {
    RbacService.RbacDetails rbac = rbacService.getUserRbac(user.getId());
    return new UserProfileResponse(
            user.getId(),
            user.getUsername(),
            requirePrimaryEmail(user.getId()).getNormalizedValue(),
            emailResponses(user.getId()),
            user.getFullName(),
            user.getStatus().name(),
            user.getAvatarUrl(),
            user.getEmailVerifiedAt(),
            accountId,
            rbac.roles(),
            rbac.permissions());
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

  private java.util.Optional<User> findUserByEmail(String normalizedEmail) {
    return userIdentifierRepository.findByTypeAndNormalizedValue(UserIdentifierType.EMAIL, normalizedEmail)
            .flatMap(identifier -> userRepo.findById(identifier.getUserId()));
  }

  private UserIdentifier requirePrimaryEmail(UUID userId) {
    return userIdentifierRepository.findByUserIdAndTypeAndPrimaryTrue(userId, UserIdentifierType.EMAIL)
            .orElseThrow(() -> new IllegalStateException("Primary email not found for user"));
  }

  private java.util.Optional<UserIdentifier> findVerifiedEmailIdentifier(String normalizedEmail) {
    return userIdentifierRepository.findByTypeAndNormalizedValue(UserIdentifierType.EMAIL, normalizedEmail)
            .filter(UserIdentifier::isVerified);
  }

  private List<UserEmailResponse> emailResponses(UUID userId) {
    return userIdentifierRepository.findAllByUserIdAndTypeOrderByPrimaryDescCreatedAtAsc(userId, UserIdentifierType.EMAIL)
            .stream()
            .map(this::toEmailResponse)
            .toList();
  }

  private UserEmailResponse toEmailResponse(UserIdentifier identifier) {
    return new UserEmailResponse(
            identifier.getId(),
            identifier.getNormalizedValue(),
            identifier.isPrimary(),
            identifier.isVerified(),
            identifier.getVerifiedAt());
  }

  private void issueVerification(UserIdentifier identifier, User user, Instant now) {
    Instant expiresAt = now.plus(EMAIL_VERIFICATION_TTL);
    String rawVerificationToken = tokenGenerator.generate();
    emailVerificationTokenRepository.save(
            EmailVerificationToken.issue(user.getId(), identifier.getId(), tokenHasher.hash(rawVerificationToken), expiresAt));
    outboxEventRepository.save(OutboxEvent.create(
            "identity.user.verification-requested.v1",
            1,
            user.getId(),
            "identity-service",
            now,
            eventSerializer.writePayload(new EventEnvelope<>(
                    UUID.randomUUID(),
                    "identity.user.verification-requested.v1",
                    1,
                    now,
                    user.getId(),
                    "identity-service",
                    new UserVerificationRequestedPayload(
                            user.getId(),
                            identifier.getNormalizedValue(),
                            user.getFullName(),
                            rawVerificationToken,
                            expiresAt)))));
  }

}
