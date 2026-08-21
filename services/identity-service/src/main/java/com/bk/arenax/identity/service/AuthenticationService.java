package com.bk.arenax.identity.service;

import com.bk.arenax.identity.dto.response.AuthTokenResponse;
import com.bk.arenax.identity.dto.response.UserEmailResponse;
import com.bk.arenax.identity.dto.response.UserProfileResponse;
import com.bk.arenax.identity.domain.RefreshSession;
import com.bk.arenax.identity.domain.User;
import com.bk.arenax.identity.domain.UserIdentifier;
import com.bk.arenax.identity.domain.UserIdentifierType;
import com.bk.arenax.identity.domain.UserStatus;
import com.bk.arenax.identity.repository.RefreshSessionRepository;
import com.bk.arenax.identity.repository.UserIdentifierRepository;
import com.bk.arenax.identity.repository.UserRepository;
import com.bk.arenax.identity.service.support.EmailNormalizationService;
import com.bk.arenax.identity.service.support.IdentityTokenGenerator;
import com.bk.arenax.identity.service.support.IdentityTokenHasher;
import com.bk.arenax.identity.service.support.UserEmailResponseMapper;
import com.bk.arenax.identity.service.support.UserProfileResponseAssembler;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

  private static final Duration LOGIN_LOCK_DURATION = Duration.ofMinutes(15);
  private static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;

  private final UserRepository userRepo;
  private final UserIdentifierRepository userIdentifierRepository;
  private final RefreshSessionRepository refreshSessionRepository;
  private final AuthenticationManager authenticationManager;
  private final com.bk.arenax.identity.infrastructure.jwt.JwtService jwtService;
  private final RbacService rbacService;
  private final IdentityTokenHasher tokenHasher;
  private final IdentityTokenGenerator tokenGenerator;
  private final EmailNormalizationService emailNormalizationService;
  private final UserEmailResponseMapper emailResponseMapper;
  private final UserProfileResponseAssembler profileAssembler;

  @Transactional(noRollbackFor = {InvalidCredentialsException.class, AccountLockedException.class})
  public LoginResult login(String email, String password, UUID accountId) {
    String normalizedEmail = emailNormalizationService.normalize(email);
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
    RefreshSession currentSession = refreshSessionRepository.findByTokenHash(tokenHasher.hash(rawRefreshToken))
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
    refreshSessionRepository.findByTokenHash(tokenHasher.hash(rawRefreshToken))
            .ifPresent(session -> session.revoke(Instant.now()));
  }

  @Transactional
  public void logoutAll(UUID userId) {
    Instant now = Instant.now();
    refreshSessionRepository.findAllByUserId(userId)
            .forEach(session -> session.revoke(now));
  }

  public long refreshTokenTtlSeconds() {
    return jwtService.getRefreshTokenTtlSeconds();
  }

  private LoginResult issueLoginResult(User user, UUID accountId, Instant now) {

    String rawRefreshToken = tokenGenerator.generate();
    RefreshSession refreshSession = refreshSessionRepository.save(
            RefreshSession.issue(
                    user.getId(),
                    tokenHasher.hash(rawRefreshToken),
                    now.plusSeconds(jwtService.getRefreshTokenTtlSeconds()),
                    accountId));

    RbacService.RbacDetails rbac = rbacService.getUserRbac(user.getId(), accountId);

    String accessToken = jwtService.issueAccessToken(
            user.getId(),
            refreshSession.getId(),
            user.getTokenVersion(),
            accountId,
            rbac.roles(),
            rbac.permissions());

    UserProfileResponse profile = profileAssembler.assemble(user, accountId);

    return new LoginResult(
            new AuthTokenResponse(
                    accessToken,
                    "Bearer",
                    jwtService.getAccessTokenTtlSeconds(),
                    profile),
            rawRefreshToken);
  }

  private java.util.Optional<User> findUserByVerifiedEmail(String normalizedEmail) {
    return findVerifiedEmailIdentifier(normalizedEmail)
            .flatMap(identifier -> userRepo.findById(identifier.getUserId()));
  }

  private java.util.Optional<UserIdentifier> findVerifiedEmailIdentifier(String normalizedEmail) {
    return userIdentifierRepository.findByTypeAndNormalizedValue(UserIdentifierType.EMAIL, normalizedEmail)
            .filter(UserIdentifier::isVerified);
  }

  public record LoginResult(AuthTokenResponse response, String refreshToken) {
  }

}