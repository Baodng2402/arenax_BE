package com.bk.arenax.domain.tenant;

import com.bk.arenax.domain.common.BaseEntity;
import com.bk.arenax.domain.subscription.SubscriptionStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "tenant_subscriptions")
public class TenantSubscription extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tenant_id", referencedColumnName = "id")
  Tenant tenant;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "plan_id", referencedColumnName = "id")
  SubscriptionPlan plan;

  @Enumerated(EnumType.STRING)
  SubscriptionStatus status;

  LocalDate startDate;
  LocalDate endDate;
  BigDecimal pricePaid;

  public boolean isCurrentlyActive() {
    return status == SubscriptionStatus.ACTIVE && !LocalDate.now().isAfter(endDate);
  }
}
