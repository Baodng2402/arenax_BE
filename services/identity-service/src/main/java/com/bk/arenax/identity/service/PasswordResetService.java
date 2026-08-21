package com.bk.arenax.identity.service;

import com.bk.arenax.identity.domain.OutboxEvent;
import com.bk.arenax.identity.domain.PasswordResetToken;
import com.bk.arenax.identity.domain.User;
import com.bk.arenax.identity.domain.UserIdentifier;
import com.bk.arenax.identity.domain.UserIdentifierType;
import com.bk.arenax.identity.messaging.UserPasswordResetRequestedPayload;
import com.bk.arenax.identity.repository.OutboxEventRepository;
import com.bk.arenax.identity.repository.PasswordResetTokenRepository;
import com.bk.arenax.identity.repository.RefreshSessionRepository;
import com.bk.arenax.identity.repository.UserIdentifierRepository;
import com.bk.arenax.identity.repository.UserRepository;
import com.bk.arenax.identity.service.support.EmailNormalizationService;
import com.bk.arenax.identity.service.support.IdentityEventPublisher;
import com.bk.arenax.identity.service.support.IdentityTokenGenerator;
import com.bk.arenax.identity.service.support.IdentityTokenHasher;
import com.bk.arenax.messaging.EventEnvelope;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PasswordResetService {
  private static final Duration PASSWORD_RESET_TTL = Duration.ofHours(1);

  private final UserRepository userRepo;
  private final UserIdentifierRepository userIdentifierRepository;
  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final RefreshSessionRepository refreshSessionRepository;
  private final PasswordEncoder passwordEncoder;
  private final IdentityTokenHasher tokenHasher;
  private final IdentityTokenGenerator tokenGenerator;
  private final EmailNormalizationService emailNormalizationService;
  private final IdentityEventPublisher eventPublisher;

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
      eventPublisher.publishPasswordResetRequested(user, identifier, rawResetToken, expiresAt, now);
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

  private Optional<UserIdentifier> findVerifiedEmailIdentifier(String normalizedEmail) {
    return userIdentifierRepository.findByTypeAndNormalizedValue(UserIdentifierType.EMAIL, normalizedEmail)
            .filter(UserIdentifier::isVerified);
  }
}