package com.bk.arenax.identity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "role_assignments")
public class RoleAssignment extends BaseEntity {

  @Id private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "account_id", nullable = false)
  private UUID accountId;

  @Column(name = "role_code", nullable = false, length = 80)
  private String roleCode;

  @PrePersist
  void assignId() {
    if (id == null) {
      id = UUID.randomUUID();
    }
  }
}
