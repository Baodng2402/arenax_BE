package com.bk.arenax.tenant.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

import com.bk.arenax.tenant.domain.enums.MembershipRole;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "memberships")
public class Membership extends BaseEntity {

  @Id private UUID id;

  @Column(name = "account_id", nullable = false)
  private UUID accountId;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private MembershipRole role;

  @PrePersist
  void assignId() {
    if (id == null) {
      id = UUID.randomUUID();
    }
  }
}
