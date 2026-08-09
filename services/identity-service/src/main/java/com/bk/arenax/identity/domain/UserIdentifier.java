package com.bk.arenax.identity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
@Table(name = "user_identifiers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserIdentifier {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserIdentifierType type;

    @Column(name = "normalized_value", nullable = false, length = 320)
    private String normalizedValue;

    @Column(name = "is_primary", nullable = false)
    private boolean primary;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

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

    public static UserIdentifier primaryEmail(UUID userId, String normalizedEmail, Instant verifiedAt) {
        UserIdentifier identifier = new UserIdentifier();
        identifier.userId = Objects.requireNonNull(userId);
        identifier.type = UserIdentifierType.EMAIL;
        identifier.normalizedValue = Objects.requireNonNull(normalizedEmail);
        identifier.primary = true;
        identifier.verifiedAt = verifiedAt;
        return identifier;
    }

    public void verify(Instant verifiedAt) {
        if (this.verifiedAt == null) {
            this.verifiedAt = Objects.requireNonNull(verifiedAt);
        }
    }
}
