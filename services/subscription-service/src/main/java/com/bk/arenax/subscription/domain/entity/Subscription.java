package com.bk.arenax.subscription.domain.entity;

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

import com.bk.arenax.subscription.domain.enums.SubscriptionPlan;
import com.bk.arenax.subscription.domain.enums.SubscriptionStatus;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "subscriptions")
public class Subscription extends BaseEntity {

  @Id private UUID id;

  @Column(name = "account_id", nullable = false, unique = true)
  private UUID accountId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private SubscriptionPlan plan;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private SubscriptionStatus status;

  @PrePersist
  void assignId() {
    if (id == null) {
      id = UUID.randomUUID();
    }
  }
}
