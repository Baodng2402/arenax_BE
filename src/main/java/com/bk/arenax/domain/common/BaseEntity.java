package com.bk.arenax.domain.common;

import jakarta.persistence.*;

import java.time.Instant;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
public abstract class BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "is_active", nullable = false)
  protected boolean isActive = Boolean.TRUE;

  @CreatedDate
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private Instant updatedAt;

  @Column(nullable = false)
  @Version
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

}
