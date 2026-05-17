package com.bk.arenax.domain.common;

import java.time.Instant;
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
public abstract class BaseEntity {
    private Long id;

    @Builder.Default
    protected boolean isActive = Boolean.TRUE;

    private Instant createdAt;
    private Instant updatedAt;

    @Builder.Default
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
