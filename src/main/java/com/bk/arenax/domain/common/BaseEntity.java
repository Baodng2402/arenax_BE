package com.bk.arenax.domain.common;

import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@SuperBuilder(toBuilder = true)
@MappedSuperclass
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    protected boolean isActive = Boolean.TRUE;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Builder.Default
    @Column(nullable = false)
    private Integer version = 0;

    public boolean isActive() {
        return Boolean.TRUE.equals(this.isActive);
    }

    public boolean isNotActive() {
        return Boolean.FALSE.equals(this.isActive);
    }

    public void deactivate() {
        this.isActive = Boolean.FALSE;
    }

    public void activate() {
        this.isActive = Boolean.TRUE;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public void updateVersion() {
        if (this.version == null) {
            this.version = 0;
        }
        this.version++;
    }
}
