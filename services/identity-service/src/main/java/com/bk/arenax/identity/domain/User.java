package com.bk.arenax.identity.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
@Entity
@Table(
    name = "users",
    uniqueConstraints = {@UniqueConstraint(name = "uk_users_email", columnNames = "email")})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
  @Id
  @Column(nullable = false, updatable = false)
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, length = 320)
  private String email;

  @Column(name = "password_hash", nullable = false, length = 255)
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private UserStatus status;

  @Column(name = "full_name", length = 120)
  private String fullName;

  @Column(name = "email_verified_at")
  private Instant emailVerifiedAt;

  @Column(length = 40)
  private String username;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "avatar_url", length = 500)
  private String avatarUrl;

  @Column(name = "last_login_at")
  private Instant lastLoginAt;

  @Column(name = "password_changed_at", nullable = false)
  private Instant passwordChangedAt;

  @Column(name = "failed_login_attempts", nullable = false)
  private int failedLoginAttempts;

  @Column(name = "locked_until")
  private Instant lockedUntil;

  @Column(name = "token_version", nullable = false)
  private int tokenVersion;

  @Version
  @Column(nullable = false)
  private Long version;

  @PrePersist
  void onCreate() {
    Instant now = Instant.now();
    createdAt = now;
    updatedAt = now;
  }

  @PreUpdate
  void onUpdate() {
    updatedAt = Instant.now();
  }

  public void recordSuccessfulLogin(Instant occurredAt) {
    lastLoginAt = Objects.requireNonNull(occurredAt);
    failedLoginAttempts = 0;
    lockedUntil = null;
  }

  public void recordFailedLogin(Instant occurredAt, int maximumAttempts, Duration lockDuration) {
    if (maximumAttempts <= 0) {
      throw new IllegalArgumentException("Maximum attempts must be positive");
    }
    if (lockDuration.isZero() || lockDuration.isNegative()) {
      throw new IllegalArgumentException("Lock duration must be positive");
    }
    failedLoginAttempts++;
    if (failedLoginAttempts >= maximumAttempts) {
      lockedUntil = occurredAt.plus(lockDuration);
      failedLoginAttempts = 0;
    }
  }

  public boolean isLockedAt(Instant now) {
    return lockedUntil != null && lockedUntil.isAfter(now);
  }

  public void changePasswordHash(String newPasswordHash, Instant changedAt) {
    passwordHash = Objects.requireNonNull(newPasswordHash);
    passwordChangedAt = Objects.requireNonNull(changedAt);
    failedLoginAttempts = 0;
    lockedUntil = null;
    tokenVersion++;
  }

  public void verifyEmail(Instant verifiedAt) {
    if (emailVerifiedAt != null) {
      return;
    }
    if (status != UserStatus.PENDING) {
      throw new IllegalStateException("Only a pending user can verify their email");
    }
    emailVerifiedAt = Objects.requireNonNull(verifiedAt);
    status = UserStatus.ACTIVE;
  }

  public void updateProfile(String fullName, String avatarUrl) {
    if (fullName != null && !fullName.isBlank()) {
      this.fullName = fullName.trim();
    }
    if (avatarUrl != null && !avatarUrl.isBlank()) {
      this.avatarUrl = avatarUrl.trim();
    }
  }

  public void setPrimaryEmail(String email, Instant verifiedAt) {
    this.email = Objects.requireNonNull(email).trim();
    this.emailVerifiedAt = verifiedAt;
  }

  public void setUsername(String username) {
    this.username = Objects.requireNonNull(username).trim();
  }

  public void clearUsername() {
    this.username = null;
  }

  public void suspend() {
    status = UserStatus.SUSPENDED;
  }

  public void deactivate() {
    status = UserStatus.DEACTIVATED;
    lockedUntil = null;
  }

  public static User register(
      String email, String passwordHash, String fullName, Instant registeredAt) {
    User user = new User();
    user.email = Objects.requireNonNull(email).trim();
    user.passwordHash = Objects.requireNonNull(passwordHash);
    user.fullName = fullName;
    user.status = UserStatus.PENDING;
    user.passwordChangedAt = Objects.requireNonNull(registeredAt);
    user.failedLoginAttempts = 0;
    user.tokenVersion = 0;
    return user;
  }
}
