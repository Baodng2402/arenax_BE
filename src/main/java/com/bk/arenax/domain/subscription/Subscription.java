package com.bk.arenax.domain.subscription;

import com.bk.arenax.domain.account.Account;
import com.bk.arenax.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "subscriptions")
public class Subscription extends BaseEntity {
  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "account_id", nullable = false, unique = true)
  private Account account;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private SubscriptionPlan plan = SubscriptionPlan.FREE;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

  @Column(name = "started_at", nullable = false)
  private Instant startedAt = Instant.now();

  @Column(name = "expires_at")
  private Instant expiresAt;

  @Column(name = "cancelled_at")
  private Instant cancelledAt;

  public Subscription(Account account, SubscriptionPlan plan, SubscriptionStatus status) {
    this.account = account;
    this.plan = plan;
    this.status = status;
    this.startedAt = Instant.now();
  }
}
