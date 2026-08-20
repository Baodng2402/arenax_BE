package com.bk.arenax.identity.service;

import com.bk.arenax.identity.domain.EmailVerificationToken;
import com.bk.arenax.identity.domain.OutboxEvent;
import com.bk.arenax.identity.domain.User;
import com.bk.arenax.identity.domain.UserIdentifier;
import com.bk.arenax.identity.domain.UserIdentifierType;
import com.bk.arenax.identity.domain.UserStatus;
import com.bk.arenax.identity.messaging.UserRegisteredPayload;
import com.bk.arenax.identity.messaging.UserVerificationRequestedPayload;
import com.bk.arenax.identity.repository.EmailVerificationTokenRepository;
import com.bk.arenax.identity.repository.OutboxEventRepository;
import com.bk.arenax.identity.repository.UserIdentifierRepository;
import com.bk.arenax.identity.repository.UserRepository;
import com.bk.arenax.identity.service.support.EmailNormalizationService;
import com.bk.arenax.identity.service.support.IdentityEventSerializer;
import com.bk.arenax.identity.service.support.IdentityTokenGenerator;
import com.bk.arenax.identity.service.support.IdentityTokenHasher;
import com.bk.arenax.messaging.EventEnvelope;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegistrationService {

  private static final Duration EMAIL_VERIFICATION_TTL = Duration.ofHours(24);

  private final UserRepository userRepo;
  private final UserIdentifierRepository userIdentifierRepository;
  private final EmailVerificationTokenRepository emailVerificationTokenRepository;
  private final OutboxEventRepository outboxEventRepository;
  private final PasswordEncoder passwordEncoder;
  private final IdentityTokenHasher tokenHasher;
  private final IdentityTokenGenerator tokenGenerator;
  private final IdentityEventSerializer eventSerializer;
  private final EmailNormalizationService emailNormalizationService;

  @Transactional
  public User register(String email, String password, String fullName){
    String normalizedEmail = emailNormalizationService.normalize(email);
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
    String rawVerificationToken = tokenGenerator.generate();
    emailVerificationTokenRepository.save(
            EmailVerificationToken.issue(savedUser.getId(), primaryEmail.getId(), tokenHasher.hash(rawVerificationToken), expiresAt));
    outboxEventRepository.save(OutboxEvent.create(
            "identity.user.verification-requested.v1",
            1,
            savedUser.getId(),
            "identity-service",
            Instant.now(),
            eventSerializer.writePayload(new EventEnvelope<>(
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
    EmailVerificationToken token = emailVerificationTokenRepository.findByTokenHash(tokenHasher.hash(rawToken))
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
              eventSerializer.writePayload(new EventEnvelope<>(
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
}