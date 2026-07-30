package com.bk.arenax.identity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "refresh_sessions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshSession {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "token_hash", nullable = false, length = 128)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public static RefreshSession issue(UUID userId, String tokenHash, Instant expiresAt) {
        RefreshSession refreshSession = new RefreshSession();
        refreshSession.userId = Objects.requireNonNull(userId);
        refreshSession.tokenHash = Objects.requireNonNull(tokenHash);
        refreshSession.expiresAt = Objects.requireNonNull(expiresAt);
        return refreshSession;
    }

    public boolean isAvailableAt(Instant now) {
        return revokedAt == null && !expiresAt.isBefore(now);
    }

    public void revoke(Instant revokedAt) {
        if (this.revokedAt == null) {
            this.revokedAt = Objects.requireNonNull(revokedAt);
        }
    }
}
