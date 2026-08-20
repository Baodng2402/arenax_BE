package com.bk.arenax.identity.service;

import com.bk.arenax.identity.controller.dto.AuthTokenResponse;
import com.bk.arenax.identity.controller.dto.UserEmailResponse;
import com.bk.arenax.identity.controller.dto.UserProfileResponse;
import com.bk.arenax.identity.controller.dto.UsernameResponse;
import com.bk.arenax.identity.domain.EmailVerificationToken;
import com.bk.arenax.identity.domain.OutboxEvent;
import com.bk.arenax.identity.domain.PasswordResetToken;
import com.bk.arenax.identity.domain.RefreshSession;
import com.bk.arenax.identity.domain.User;
import com.bk.arenax.identity.domain.UserIdentifier;
import com.bk.arenax.identity.domain.UserIdentifierType;
import com.bk.arenax.identity.domain.UserStatus;
import com.bk.arenax.messaging.EventEnvelope;
import com.bk.arenax.identity.messaging.UserPasswordResetRequestedPayload;
import com.bk.arenax.identity.messaging.UserRegisteredPayload;
import com.bk.arenax.identity.messaging.UserVerificationRequestedPayload;
import com.bk.arenax.identity.repository.EmailVerificationTokenRepository;
import com.bk.arenax.identity.repository.OutboxEventRepository;
import com.bk.arenax.identity.repository.PasswordResetTokenRepository;
import com.bk.arenax.identity.repository.RefreshSessionRepository;
import com.bk.arenax.identity.repository.UserIdentifierRepository;
import com.bk.arenax.identity.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
public class UserService {
  private static final Duration EMAIL_VERIFICATION_TTL = Duration.ofHours(24);
  private static final Duration PASSWORD_RESET_TTL = Duration.ofHours(1);
  private static final Duration LOGIN_LOCK_DURATION = Duration.ofMinutes(15);
  private static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private final UserRepository userRepo;
  private final UserIdentifierRepository userIdentifierRepository;
  private final EmailVerificationTokenRepository emailVerificationTokenRepository;
  private final OutboxEventRepository outboxEventRepository;
  private final RefreshSessionRepository refreshSessionRepository;
  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final ObjectMapper objectMapper;
  private final AuthenticationManager authenticationManager;
  private final com.bk.arenax.identity.infrastructure.jwt.JwtService jwtService;
  private final RbacService rbacService;

  @Transactional
  public User register(String email, String password, String fullName){
    String normalizedEmail = normalizeEmail(email);
    if(userIdentifierRepository.existsByTypeAndNormalizedValue(UserIdentifierType.EMAIL, normalizedEmail)){
      throw new IllegalArgumentException("Email already exists");
    }

    User user = User.register(
            normalizedEmail,
            passwordEncoder.encode(password),
            fullName == null ? null : fullName.trim(),
            Instant.now());
    User savedUser = userRepo.save(user);
    UserIdentifier primaryEmail = userIdentifierRepository.save(
            UserIdentifier.primaryEmail(savedUser.getId(), normalizedEmail, null));

    Instant expiresAt = Instant.now().plus(EMAIL_VERIFICATION_TTL);
    String rawVerificationToken = generateOpaqueToken();
    emailVerificationTokenRepository.save(
            EmailVerificationToken.issue(savedUser.getId(), primaryEmail.getId(), hashToken(rawVerificationToken), expiresAt));
    outboxEventRepository.save(OutboxEvent.create(
            "identity.user.verification-requested.v1",
            1,
            savedUser.getId(),
            "identity-service",
            Instant.now(),
            writePayload(new EventEnvelope<>(
                    UUID.randomUUID(),
                    "identity.user.verification-requested.v1",
                    1,
                    Instant.now(),
                    savedUser.getId(),
                    "identity-service",
                    new UserVerificationRequestedPayload(
                            savedUser.getId(),
                            primaryEmail.getNormalizedValue(),
                            savedUser.getFullName(),
                            rawVerificationToken,
                            expiresAt)))));
    return savedUser;
  }

  @Transactional
  public void verifyEmail(String rawToken) {
    Instant now = Instant.now();
    EmailVerificationToken token = emailVerificationTokenRepository.findByTokenHash(hashToken(rawToken))
            .orElseThrow(() -> new IllegalArgumentException("Invalid verification token"));
    if (!token.isAvailableAt(now)) {
      throw new IllegalStateException("Verification token is no longer valid");
    }

    User user = userRepo.findById(token.getUserId())
            .orElseThrow(() -> new IllegalStateException("User not found for verification token"));
    UserIdentifier identifier = userIdentifierRepository.findById(token.getUserIdentifierId())
            .orElseThrow(() -> new IllegalStateException("Identifier not found for verification token"));

    boolean activatingUser = user.getStatus() == UserStatus.PENDING && identifier.isPrimary();
    token.consume(now);
    identifier.verify(now);
    if (activatingUser) {
      user.verifyEmail(now);
      outboxEventRepository.save(OutboxEvent.create(
              "identity.user.registered.v2",
              2,
              user.getId(),
              "identity-service",
              now,
              writePayload(new EventEnvelope<>(
                      UUID.randomUUID(),
                      "identity.user.registered.v2",
                      2,
                      now,
                      user.getId(),
                      "identity-service",
                      new UserRegisteredPayload(
                              user.getId(),
                              user.getFullName())))));
      return;
    }

    if (identifier.isPrimary()) {
      user.setPrimaryEmail(identifier.getNormalizedValue(), identifier.getVerifiedAt());
    }
  }

  @Transactional(noRollbackFor = {InvalidCredentialsException.class, AccountLockedException.class})
  public LoginResult login(String email, String password, UUID accountId) {
    String normalizedEmail = normalizeEmail(email);
    User user = findUserByVerifiedEmail(normalizedEmail).orElse(null);
    Instant now = Instant.now();

    if (user != null && user.isLockedAt(now)) {
      throw new AccountLockedException();
    }

    if (user != null && (user.getStatus() == UserStatus.SUSPENDED || user.getStatus() == UserStatus.DEACTIVATED)) {
      throw new AccountStatusException(user.getStatus());
    }

    try {
      authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(normalizedEmail, password));
    } catch (AuthenticationException exception) {
      if (user != null && user.getStatus() == com.bk.arenax.identity.domain.UserStatus.ACTIVE && !user.isLockedAt(now)) {
        user.recordFailedLogin(now, MAX_FAILED_LOGIN_ATTEMPTS, LOGIN_LOCK_DURATION);
        if (user.isLockedAt(now)) {
          throw new AccountLockedException();
        }
      }
      throw new InvalidCredentialsException();
    } catch (RuntimeException exception) {
      throw new AuthenticationServiceException("Authentication failed", exception);
    }

    user = findUserByVerifiedEmail(normalizedEmail)
            .orElseThrow(() -> new IllegalStateException("Authenticated user no longer exists"));
    user.recordSuccessfulLogin(now);

    return issueLoginResult(user, accountId, now);
  }

  @Transactional(noRollbackFor = IllegalStateException.class)
  public LoginResult refresh(String rawRefreshToken) {
    Instant now = Instant.now();
    RefreshSession currentSession = refreshSessionRepository.findByTokenHash(hashToken(rawRefreshToken))
            .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
    if (!currentSession.isAvailableAt(now)) {
      if (currentSession.getRevokedAt() != null) {
        refreshSessionRepository.findAllByUserId(currentSession.getUserId())
                .forEach(session -> session.revoke(now));
        throw new IllegalStateException("Refresh token reuse detected; all sessions revoked");
      }
      throw new IllegalArgumentException("Invalid refresh token");
    }

    User user = userRepo.findById(currentSession.getUserId())
            .orElseThrow(() -> new IllegalStateException("User not found for refresh session"));
    if (user.getStatus() == UserStatus.SUSPENDED || user.getStatus() == UserStatus.DEACTIVATED) {
      throw new AccountStatusException(user.getStatus());
    }
    currentSession.revoke(now);
    user.recordSuccessfulLogin(now);

    return issueLoginResult(user, currentSession.getAccountId(), now);
  }

  @Transactional
  public void logout(String rawRefreshToken) {
    refreshSessionRepository.findByTokenHash(hashToken(rawRefreshToken))
            .ifPresent(session -> session.revoke(Instant.now()));
  }

  @Transactional
  public void logoutAll(UUID userId) {
    Instant now = Instant.now();
    refreshSessionRepository.findAllByUserId(userId)
            .forEach(session -> session.revoke(now));
  }

  @Transactional
  public void requestPasswordReset(String email) {
    findVerifiedEmailIdentifier(normalizeEmail(email)).ifPresent(identifier -> {
      User user = userRepo.findById(identifier.getUserId())
              .orElseThrow(() -> new IllegalStateException("User not found for password reset identifier"));
      Instant now = Instant.now();
      Instant expiresAt = now.plus(PASSWORD_RESET_TTL);
      String rawResetToken = generateOpaqueToken();
      passwordResetTokenRepository.save(
              PasswordResetToken.issue(user.getId(), hashToken(rawResetToken), expiresAt));
      outboxEventRepository.save(OutboxEvent.create(
              "identity.user.password-reset-requested.v1",
              1,
              user.getId(),
              "identity-service",
              now,
              writePayload(new EventEnvelope<>(
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
    PasswordResetToken token = passwordResetTokenRepository.findByTokenHash(hashToken(rawToken))
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

  private LoginResult issueLoginResult(User user, UUID accountId, Instant now) {

    String rawRefreshToken = generateOpaqueToken();
    RefreshSession refreshSession = refreshSessionRepository.save(
            RefreshSession.issue(
                    user.getId(),
                    hashToken(rawRefreshToken),
                    now.plusSeconds(jwtService.getRefreshTokenTtlSeconds()),
                    accountId));

    RbacService.RbacDetails rbac = rbacService.getUserRbac(user.getId());

    String accessToken = jwtService.issueAccessToken(
            user.getId(),
            refreshSession.getId(),
            user.getTokenVersion(),
            accountId,
            rbac.roles(),
            rbac.permissions());

    return new LoginResult(
            new AuthTokenResponse(
                     accessToken,
                     "Bearer",
                     jwtService.getAccessTokenTtlSeconds(),
                      new UserProfileResponse(
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
                             rbac.permissions())),
             rawRefreshToken);
  }

  public long refreshTokenTtlSeconds() {
    return jwtService.getRefreshTokenTtlSeconds();
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
    String normalizedEmail = normalizeEmail(email);
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

  private String normalizeEmail(String email){
    return email.trim().toLowerCase(Locale.ROOT);
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

  private java.util.Optional<User> findUserByVerifiedEmail(String normalizedEmail) {
    return findVerifiedEmailIdentifier(normalizedEmail)
            .flatMap(identifier -> userRepo.findById(identifier.getUserId()));
  }

  private java.util.Optional<UserIdentifier> findVerifiedEmailIdentifier(String normalizedEmail) {
    return userIdentifierRepository.findByTypeAndNormalizedValue(UserIdentifierType.EMAIL, normalizedEmail)
            .filter(UserIdentifier::isVerified);
  }

  private UserIdentifier requirePrimaryEmail(UUID userId) {
    return userIdentifierRepository.findByUserIdAndTypeAndPrimaryTrue(userId, UserIdentifierType.EMAIL)
            .orElseThrow(() -> new IllegalStateException("Primary email not found for user"));
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
    String rawVerificationToken = generateOpaqueToken();
    emailVerificationTokenRepository.save(
            EmailVerificationToken.issue(user.getId(), identifier.getId(), hashToken(rawVerificationToken), expiresAt));
    outboxEventRepository.save(OutboxEvent.create(
            "identity.user.verification-requested.v1",
            1,
            user.getId(),
            "identity-service",
            now,
            writePayload(new EventEnvelope<>(
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

  private String generateOpaqueToken() {
    byte[] bytes = new byte[32];
    SECURE_RANDOM.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private String hashToken(String rawToken) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return Base64.getUrlEncoder().withoutPadding()
              .encodeToString(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 not available", exception);
    }
  }

  private String writePayload(EventEnvelope<?> envelope) {
    try {
      return objectMapper.writeValueAsString(envelope);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Failed to serialize identity event payload", exception);
    }
  }

  public record LoginResult(AuthTokenResponse response, String refreshToken) {
  }

}
