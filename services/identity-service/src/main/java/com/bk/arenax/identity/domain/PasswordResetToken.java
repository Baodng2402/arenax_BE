package com.bk.arenax.identity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(name = "password_reset_tokens")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PasswordResetToken {

  @Id
  @Column(nullable = false, updatable = false)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "token_hash", nullable = false, length = 128)
  private String tokenHash;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "consumed_at")
  private Instant consumedAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @PrePersist
  void onCreate() {
    Instant now = Instant.now();
    id = id == null ? UUID.randomUUID() : id;
    createdAt = now;
    updatedAt = now;
  }

  @PreUpdate
  void onUpdate() {
    updatedAt = Instant.now();
  }

  public boolean isAvailableAt(Instant now) {
    return consumedAt == null && expiresAt.isAfter(now);
  }

  public void consume(Instant consumedAt) {
    if (this.consumedAt == null) {
      this.consumedAt = consumedAt;
    }
  }

  public static PasswordResetToken issue(UUID userId, String tokenHash, Instant expiresAt) {
    PasswordResetToken token = new PasswordResetToken();
    token.userId = userId;
    token.tokenHash = tokenHash;
    token.expiresAt = expiresAt;
    return token;
  }
}
